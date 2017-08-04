package com.peregrine.nodejs.osgi;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.security.Privilege;

@Component(immediate = true)
public class CreateDistributionUsersAndPermissions {

    public static final String NODE_JS_SERVICE_USER_NAME = "nodejs-service-user";

    final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    SlingRepository slingRepository;

    @Activate
    public void activate() throws Exception {

        try {
            log.trace("Sling Repository: '{}'", slingRepository);
            Session session = slingRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));

            JackrabbitSession jackrabitSession  = (JackrabbitSession) session;
            UserManager userManager = jackrabitSession.getUserManager();
            User defaultAgentUser = createOrGetServiceUser(userManager, NODE_JS_SERVICE_USER_NAME);

//            if (defaultAgentUser != null) {
//                AccessControlUtils.addAccessControlEntry(session, "/var/sling/distribution/packages", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
//                AccessControlUtils.addAccessControlEntry(session, "/libs/sling/distribution", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
//                AccessControlUtils.addAccessControlEntry(session, "/etc/distribution", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
//                AccessControlUtils.addAccessControlEntry(session, null, defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
//                // We need the 'distribution-agent-user' user to have full access to the nodes we distribution from and to to set the replication properties
//                AccessControlUtils.addAccessControlEntry(session, "/content", defaultAgentUser.getPrincipal(), new String[]{ Privilege.JCR_ALL }, true);
//            }
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
