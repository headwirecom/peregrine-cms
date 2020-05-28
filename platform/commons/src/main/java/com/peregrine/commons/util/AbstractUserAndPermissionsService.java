package com.peregrine.commons.util;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.Privilege;

import java.security.Principal;

import static com.peregrine.commons.util.PerConstants.ADMIN_USER;
import static com.peregrine.commons.util.PerUtil.loginService;

/**
 * Abstract Base Class to create System Users for Service Logins
 * and setting permissions. If it cannot login as default admin
 * it will try to obtain the Resource Resolver using the given
 * Service name and tests the permissions
 */
public abstract class AbstractUserAndPermissionsService {

    private static final String DEFAULT_USER_NAME = ADMIN_USER;

    final Logger log = LoggerFactory.getLogger(getClass());

    /** @return Sling Repository to login as admin and to create the System User **/
    public abstract SlingRepository getSlingRepository();
    /** @return Resource Resolver Factory to obtain service resource resolver to test permisions **/
    public abstract ResourceResolverFactory getResourceResolverFactory();

    protected void handleUserAndPermissions(String userName, String subServiceName) throws Exception {
        Session session = null;
        User defaultAgentUser = null;
        ResourceResolver resourceResolver = null;
        try {
            try {
                session = getSlingRepository().login(new SimpleCredentials(DEFAULT_USER_NAME, DEFAULT_USER_NAME.toCharArray()));
            } catch(LoginException e) {
                log.debug("Failed to login as default 'admin'");
            }
            // If we got a session as default admin then we create the System User and its permissions
            if(session != null) {
                try {
                    JackrabbitSession jackrabitSession = (JackrabbitSession) session;
                    UserManager userManager = jackrabitSession.getUserManager();
                    defaultAgentUser = createOrGetServiceUser(userManager, userName);
                    session.save();
                } catch(RepositoryException e) {
                    log.error("Failed to create System User for: '" + userName + "'", e);
                    return;
                }
                try {
                    if(defaultAgentUser != null) {
                        createPermissions(session, defaultAgentUser.getPrincipal());
                    }
                    session.save();
                } catch(RepositoryException e){
                    log.error("Failed to create Permissions on User: '" + userName + "'", e);
                }
            } else {
                // If we could not login as default admin then we just check the user and permissions
                // Try to find the Service Resource Resolver and check if the permissions are in place
                try {
                    resourceResolver = loginService(getResourceResolverFactory(), subServiceName);
                } catch(org.apache.sling.api.resource.LoginException e) {
                    log.error("Cannot obtain Service Resource Resolver of the Sub Service: '" + subServiceName +"' -> Create that user and set the necessary permissions", e);
                }
                if(resourceResolver != null) {
                    session = resourceResolver.adaptTo(Session.class);
                    if(session != null) {
                        testPermissions(session, userName);
                    }
                }
            }
        } finally {
            if(session != null) {
                session.logout();
            }
            if(resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    /**
     * Create the necessary permissions with the given session and user principal
     * @param session Session to use to create the permissions
     * @param principal User Principal adding the permissions to
     * @throws RepositoryException If the creation of the permissions failed
     */
    protected abstract void createPermissions(Session session, Principal principal) throws RepositoryException;

    /**
     * Test Permissions if they are set
     * @param session Session to test the permissions against
     * @param userName Name of the user to look for the permissions
     */
    protected abstract void testPermissions(Session session, String userName);

    /**
     * Creates a Service User if it does not exist
     * @param userManager User Manager to be used to create the System User
     * @param serviceUserName Name of the System User to be created
     * @return System User that was created
     * @throws RepositoryException If the creation of the System User failed
     */
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

    /**
     * Checks if the permissions exists
     * @param session Session to obtain the permissions from
     * @param path Path of the resource to check for the permissions
     * @param userName User Name having the permission
     * @param permission Name of the permission (like 'jcr:all') to check
     */
    protected void checkUserPermission(Session session, String path, String userName, String permission) {
        try {
            JackrabbitAccessControlList accessControlList = AccessControlUtils.getAccessControlList(session, path);
            AccessControlEntry[] accessControlEntries = accessControlList.getAccessControlEntries();
            boolean found = false;
            for(AccessControlEntry entry : accessControlEntries) {
                JackrabbitAccessControlEntry jackrabbitAccessControlEntry = (JackrabbitAccessControlEntry) entry;
                String principalName = entry.getPrincipal().getName();
                log.trace("Got Access Control Entry Principal Name: '{}', allowed: '{}'", principalName, jackrabbitAccessControlEntry.isAllow());
                if(principalName.equalsIgnoreCase(userName)) {
                    Privilege[] privileges = entry.getPrivileges();
                    for(Privilege privilege : privileges) {
                        String privilegeName = privilege.getName();
                        log.trace("Got Access Control Priviledge Name: '{}'", privilegeName);
                        if(privilegeName.equals(permission)) {
                            if(jackrabbitAccessControlEntry.isAllow()) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(!found) {
                log.error("Permission: '{}' was not found for User: '{}' and Path: '{}'", permission, userName, path);
            }
        } catch(RepositoryException e) {
            log.error("Failed to Check Permissions for User: '{}' and Paath: '{}'", userName, path);
            log.error("Failed to Check Permissions", e);
        }
    }
}
