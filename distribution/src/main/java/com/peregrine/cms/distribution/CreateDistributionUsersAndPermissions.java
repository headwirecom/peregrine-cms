package com.peregrine.cms.distribution;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;

import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;

@Component(immediate = true)
public class CreateDistributionUsersAndPermissions {

    public static final String DISTRIBUTION_AGENT_USER = "distribution-agent-user";

    final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    SlingRepository slingRepository;

    @Activate
    public void activate() throws Exception {

        try {
            Session session = slingRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));

            JackrabbitSession jackrabitSession  = (JackrabbitSession) session;
            UserManager userManager = jackrabitSession.getUserManager();
            User defaultAgentUser = createOrGetServiceUser(userManager, DISTRIBUTION_AGENT_USER);

            if (defaultAgentUser != null) {
                AccessControlUtils.addAccessControlEntry(session, "/var/sling/distribution/packages", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
                AccessControlUtils.addAccessControlEntry(session, "/libs/sling/distribution", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
                AccessControlUtils.addAccessControlEntry(session, "/etc/distribution", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
                AccessControlUtils.addAccessControlEntry(session, null, defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
                // We need the 'distribution-agent-user' user to have full access to the nodes we distribution from and to to set the replication properties
                AccessControlUtils.addAccessControlEntry(session, "/content", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
            }
            session.save();
            session.logout();
        } catch (Throwable t) {
            log.error("cannot create user", t);
        }
    }

    private User createOrGetServiceUser(UserManager userManager, String serviceUserName) throws RepositoryException {
        Authorizable serviceUser = userManager.getAuthorizable(serviceUserName);

        if (serviceUser == null) {
            try {
                serviceUser = userManager.createSystemUser(serviceUserName, null);
                log.info("created system user {}", serviceUserName);

            } catch (Throwable t) {
                serviceUser = userManager.createUser(serviceUserName, "123");
                log.info("created regular user {}", serviceUserName);
            }
        }

        return (User) serviceUser;

    }
}
