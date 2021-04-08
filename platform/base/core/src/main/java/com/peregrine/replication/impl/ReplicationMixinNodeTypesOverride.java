package com.peregrine.replication.impl;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.replication.ReplicationUtil.setReplicationPrimaryNodeTypes;
import static com.peregrine.commons.util.PerConstants.DISTRIBUTION_SUB_SERVICE;
import static com.peregrine.commons.util.PerUtil.loginService;
import static org.osgi.service.component.annotations.ConfigurationPolicy.OPTIONAL;

/**
 * This service provides the opportunity to override the Node Types that will
 * receive the Peregrine Replication Mixin. If not provided is the list of
 * Sub Types in the Mixin.
 */
@Component(
    configurationPolicy = OPTIONAL,
    immediate = true
)
@Designate(ocd = ReplicationMixinNodeTypesOverride.Configuration.class)
public class ReplicationMixinNodeTypesOverride {
    @ObjectClassDefinition(
        name = "Peregrine: Replication Mixin Node Types Override",
        description = "This service does allow to specify all node types that should contain the Replication Mixin " +
            "and overrides the ones taken from Node Type configuration"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Description",
            description = "Description of this Replication Service"
        )
        String description();
        @AttributeDefinition(
            name = "Replication Node Type",
            description = "All Node Type Names that will have the Peregrine Replication Mixin added during replication"
        )
        String[] replicationNodeTypes();
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }

    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;

    private void setup(Configuration configuration) {
        if(configuration == null || configuration.replicationNodeTypes() == null) {
            log.debug("No Replication Mixin Override Provided -> ignored");
            return;
        }
        List<String> replicationNodeTypes = Arrays.asList(configuration.replicationNodeTypes());
        setReplicationPrimaryNodeTypes(replicationNodeTypes);
        try (ResourceResolver resourceResolver = loginService(resourceResolverFactory, DISTRIBUTION_SUB_SERVICE)) {
            log.trace("Resource Resolver: '{}'", resourceResolver);
            NodeTypeManager manager = resourceResolver.adaptTo(Session.class).getWorkspace().getNodeTypeManager();
            for (String replicationNodeType : replicationNodeTypes) {
                try {
                    manager.getNodeType(replicationNodeType);
                } catch (NoSuchNodeTypeException e) {
                    log.warn("Replication Node Type does not exist: '{}'", replicationNodeType, e);
                }
            }
        } catch (final LoginException | RepositoryException e) {
            log.warn("Replication Node Types could not be checked", e);
        }
    }
}
