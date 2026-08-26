package com.peregrine.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.security.Principal;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.JackrabbitSession;
import javax.jcr.security.AccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peregrine.functions.FunctionRunner.FunctionException;

/**
 * The repository half of the functions runtime: a NARROW, per-tenant store
 * that a function can read and write.
 *
 * Functions deliberately have no repository access - they are public-grade
 * content that anyone with authoring rights can edit, so handing them the
 * request's session (or an admin one) would be an escalation. But a form
 * handler has to put the form somewhere, so this exists instead of a general
 * repository API:
 *
 *  - it works only under the tenant's OWN objects tree, and only under the
 *    subpath that tenant's OSGi configuration opts in ("storageRoot");
 *    a tenant with no storageRoot configured cannot write at all;
 *  - it writes per:Object nodes and sling:OrderedFolder parents, nothing
 *    else - no pages, no /apps, no node types that carry scripts;
 *  - names are constrained to [a-z0-9-] so a path cannot escape sideways,
 *    and depth is capped;
 *  - values are flattened to strings: a function cannot construct arbitrary
 *    JCR structure through it.
 *
 * The session is a dedicated service user (functions-storage), so what a
 * function may touch is also enforced by repository ACLs, not just by this
 * class.
 */
@Component(service = FunctionStorage.class)
public class FunctionStorage {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionStorage.class);
    private static final String SUB_SERVICE = "functions-storage";
    private static final String OBJECT_PRIMARY_TYPE = "per:Object";
    private static final String FOLDER_PRIMARY_TYPE = "sling:OrderedFolder";
    private static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    private static final String JCR_TITLE = "jcr:title";
    private static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    private static final String OBJECT_PATH = "objectPath";
    private static final int MAX_DEPTH = 6;
    private static final int MAX_LIST = 500;
    private static final String ANONYMOUS = "anonymous";

    @Reference
    private ResourceResolverFactory resolverFactory;

    /** A name a function may use for a folder or an object. */
    static boolean isSaneName(String name) {
        return name != null && name.matches("[a-z0-9][a-z0-9-]{0,63}");
    }

    /**
     * Resolve a function-supplied relative path against the tenant's storage
     * root. Returns null when the function may not touch it.
     */
    private String resolve(FunctionRunner.TenantSettings settings, String relative) {
        final String root = settings.storageRoot();
        if (root == null || root.isEmpty()) {
            throw new FunctionException(403,
                    "this site has no storage root configured - set storageRoot in its functions settings");
        }
        if (relative == null || relative.isEmpty()) {
            return root;
        }
        final String[] segments = relative.replaceAll("^/+", "").split("/");
        if (segments.length > MAX_DEPTH) {
            throw new FunctionException(400, "storage path is too deep");
        }
        final StringBuilder path = new StringBuilder(root);
        for (final String segment : segments) {
            if (!isSaneName(segment)) {
                throw new FunctionException(400,
                        "storage path segment '" + segment + "' is not a-z, 0-9 or dash");
            }
            path.append('/').append(segment);
        }
        return path.toString();
    }

    private ResourceResolver session() {
        try {
            return resolverFactory.getServiceResourceResolver(
                    Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, (Object) SUB_SERVICE));
        } catch (final LoginException e) {
            LOG.warn("no '{}' service session - function storage is unavailable", SUB_SERVICE, e);
            throw new FunctionException(500, "function storage is not available on this instance");
        }
    }

    /** Everything authored on the node, minus the plumbing. */
    private static Map<String, Object> readValues(Resource resource) {
        final Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", resource.getName());
        final ValueMap values = resource.getValueMap();
        values.forEach((key, value) -> {
            if (key.startsWith("jcr:") || key.startsWith("sling:") || key.equals(OBJECT_PATH)) {
                return;
            }
            out.put(key, value instanceof Calendar
                    ? String.format("%1$tFT%1$tT", (Calendar) value)
                    : String.valueOf(value));
        });
        return out;
    }

    public Map<String, Object> get(FunctionRunner.TenantSettings settings, String relative) {
        final String path = resolve(settings, relative);
        try (ResourceResolver resolver = session()) {
            final Resource resource = resolver.getResource(path);
            return resource == null ? null : readValues(resource);
        }
    }

    public List<Map<String, Object>> list(FunctionRunner.TenantSettings settings, String relative) {
        final String path = resolve(settings, relative);
        final List<Map<String, Object>> out = new ArrayList<>();
        try (ResourceResolver resolver = session()) {
            final Resource parent = resolver.getResource(path);
            if (parent == null) {
                return out;
            }
            for (final Resource child : parent.getChildren()) {
                out.add(readValues(child));
                if (out.size() >= MAX_LIST) {
                    break;
                }
            }
        }
        return out;
    }

    /**
     * Create or update one object, making the folders on the way. Values are
     * stored as strings; a null value removes the property.
     */
    public Map<String, Object> put(FunctionRunner.TenantSettings settings, String relative,
                                   Map<String, Object> values, String definition) {
        final String path = resolve(settings, relative);
        if (path.equals(settings.storageRoot())) {
            throw new FunctionException(400, "cannot write the storage root itself");
        }
        try (ResourceResolver resolver = session()) {
            final Resource parent = ensureFolders(resolver, settings.storageRoot(),
                    path.substring(0, path.lastIndexOf('/')));
            final String name = path.substring(path.lastIndexOf('/') + 1);
            Resource object = resolver.getResource(path);
            if (object == null) {
                final Map<String, Object> props = new LinkedHashMap<>();
                props.put(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
                props.put(JCR_TITLE, name);
                if (definition != null && !definition.isEmpty()) {
                    // the same back-pointer the admin writes, so an object a
                    // function created opens in the object editor like any other
                    props.put(OBJECT_PATH, definition);
                    if (definition.startsWith("/apps/")) {
                        props.put(SLING_RESOURCE_TYPE, definition);
                    }
                }
                object = resolver.create(parent, name, props);
            }
            final ModifiableValueMap valueMap = object.adaptTo(ModifiableValueMap.class);
            if (valueMap == null) {
                throw new FunctionException(500, "storage node is not writable: " + path);
            }
            values.forEach((key, value) -> {
                if (key.startsWith("jcr:") || key.startsWith("sling:") || key.equals(OBJECT_PATH)) {
                    return;
                }
                if (value == null) {
                    valueMap.remove(key);
                } else {
                    valueMap.put(key, String.valueOf(value));
                }
            });
            resolver.commit();
            return readValues(resolver.getResource(path));
        } catch (final PersistenceException e) {
            LOG.warn("function storage write failed for {}", path, e);
            throw new FunctionException(500, "could not write " + relative + ": " + e.getMessage());
        }
    }

    public boolean remove(FunctionRunner.TenantSettings settings, String relative) {
        final String path = resolve(settings, relative);
        if (path.equals(settings.storageRoot())) {
            throw new FunctionException(400, "cannot delete the storage root");
        }
        try (ResourceResolver resolver = session()) {
            final Resource resource = resolver.getResource(path);
            if (resource == null) {
                return false;
            }
            resolver.delete(resource);
            resolver.commit();
            return true;
        } catch (final PersistenceException e) {
            LOG.warn("function storage delete failed for {}", path, e);
            throw new FunctionException(500, "could not delete " + relative + ": " + e.getMessage());
        }
    }

    /**
     * Close the storage root to anonymous readers.
     *
     * Form submissions are not site content: a deep JSON listing of the
     * folder would otherwise hand every stored address to any visitor who
     * asks for it. Denying the anonymous principal read on the root covers
     * every bucket and record underneath, while leaving the data visible to
     * signed-in authors - which is what the admin screens run as.
     */
    private void denyAnonymous(ResourceResolver resolver, String path) {
        try {
            final Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                throw new FunctionException(500, "no JCR session for function storage");
            }
            Principal anonymous = null;
            if (session instanceof JackrabbitSession) {
                anonymous = ((JackrabbitSession) session).getPrincipalManager().getPrincipal(ANONYMOUS);
            }
            if (anonymous == null) {
                // the service session may not be allowed to read the principal
                // manager; Oak resolves a deny entry by principal NAME just as
                // well, so carry on with a plain named principal
                anonymous = () -> ANONYMOUS;
            }
            final AccessControlManager acm = session.getAccessControlManager();
            final JackrabbitAccessControlList acl =
                    AccessControlUtils.getAccessControlList(session, path);
            if (acl == null) {
                throw new FunctionException(500, "no access control list applies to " + path);
            }
            final boolean added = acl.addEntry(anonymous,
                    new Privilege[] { acm.privilegeFromName(Privilege.JCR_READ) }, false);
            if (!added) {
                LOG.error("repository refused the anonymous deny entry on {}", path);
                throw new FunctionException(500, "storage could not be secured - refusing to write");
            }
            acm.setPolicy(path, acl);
            session.save();
            LOG.info("function storage root {} is closed to anonymous readers", path);
        } catch (final RepositoryException e) {
            LOG.error("could not close {} to anonymous readers", path, e);
            throw new FunctionException(500, "storage could not be secured - refusing to write");
        }
    }

    /** Make every missing folder between the storage root and a path. */
    private Resource ensureFolders(ResourceResolver resolver, String root, String path)
            throws PersistenceException {
        Resource resource = resolver.getResource(path);
        if (resource != null) {
            /*
             * The root can EXIST without ever having passed through this
             * class: createTenant copies the theme's whole objects tree, so a
             * theme that was used for live testing hands every new site a
             * ready-made storage root - created by the copy, carrying no ACL.
             * "Closed the moment it is created" therefore has to be "closed
             * the moment it is USED": cheap read check on every write, the
             * deny written only when it is missing.
             */
            if (path.equals(root)) {
                ensureRootClosed(resolver, root);
            }
            return resource;
        }
        final Resource parent = ensureFolders(resolver, root, path.substring(0, path.lastIndexOf('/')));
        final Map<String, Object> props = new LinkedHashMap<>();
        props.put(JCR_PRIMARY_TYPE, FOLDER_PRIMARY_TYPE);
        final Resource created = resolver.create(parent, path.substring(path.lastIndexOf('/') + 1), props);
        if (path.equals(root)) {
            resolver.commit();
            denyAnonymous(resolver, path);
        }
        return created;
    }

    /** Add the anonymous deny to a root that predates this class, once. */
    private void ensureRootClosed(ResourceResolver resolver, String root) {
        try {
            final Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                throw new FunctionException(500, "no JCR session for function storage");
            }
            final JackrabbitAccessControlList acl =
                    AccessControlUtils.getAccessControlList(session, root);
            if (acl != null) {
                for (final AccessControlEntry entry : acl.getAccessControlEntries()) {
                    if (ANONYMOUS.equals(entry.getPrincipal().getName())) {
                        return;                       // already closed
                    }
                }
            }
        } catch (final RepositoryException e) {
            LOG.error("could not inspect the ACL on {}", root, e);
            throw new FunctionException(500, "storage could not be secured - refusing to write");
        }
        denyAnonymous(resolver, root);
    }

}
