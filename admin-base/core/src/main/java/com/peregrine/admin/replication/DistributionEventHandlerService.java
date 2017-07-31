package com.peregrine.admin.replication;

import com.peregrine.commons.util.PerConstants;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.distribution.event.DistributionEventProperties;
import org.apache.sling.distribution.event.DistributionEventTopics;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.DISTRIBUTION_SUB_SERVICE;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.loginService;

@Component(
    service = EventHandler.class,
    immediate = true,
    property = {
        Constants.SERVICE_VENDOR + PerUtil.EQUALS + PerUtil.PER_PREFIX + "Replication Event Handler",
        EventConstants.EVENT_TOPIC + PerUtil.EQUALS + DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED,
        EventConstants.EVENT_TOPIC + PerUtil.EQUALS + DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED
    }
)
/**
 * This service will react to Sling Distribution Events to set the properties
 * on the replicated nodes both on the Author and Publish side
 *
 * In order for this to work there must be a Service User defined: 'defaultAgentService'
 * with write access to wherever properties are written too.
 */
public class DistributionEventHandlerService
    implements EventHandler
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void handleEvent(Event event) {
        final String topic = event.getTopic();
        String kind;
        if(DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED.equals(topic)) {
            // Forward Agent Event
            // Check the expected properties
            if(!checkEventProperties(event, "distribution.type=ADD", "distribution.component.kind=AGENT", "event.topics=" + DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED)) {
                // Ignore -> Done
                logEvent("Received unexpected Agent Event", event);
                return;
            }
            kind = "AGENT";
        } else
        if(DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED.equals(topic)) {
            // Forward Agent Event
            // Check the expected properties
            if(!checkEventProperties(event, "distribution.type=ADD", "distribution.component.kind=IMPORTER", "event.topics=" + DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED)) {
                // Ignore -> Done
                logEvent("Received unexpected Importer Event", event);
                return;
            }
            kind = "IMPORTER";
        } else {
            log.debug("Received unhandled event: '{}'", event);
            return;
        }
        Object value = event.getProperty(DistributionEventProperties.DISTRIBUTION_PATHS);
        if(value instanceof String[]) {
            String[] paths = (String[]) value;
            for(String path: paths) {
                log.debug("Set Replication Properties on: '{}'", path);
                try {
                    setReplicationProperties(path, kind);
                } catch(Throwable t) {
                    log.warn("Set Replication Properties failed", t);
                }
            }
        }
    }

    private void setReplicationProperties(String path, String kind) {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = loginService(resourceResolverFactory, DISTRIBUTION_SUB_SERVICE);
            log.debug("Resource Resolver: '{}'", resourceResolver);
            Resource resource = getResource(resourceResolver, path);
            log.debug("Resource for Path: '{}': '{}'", path, resource);
            if(resource != null) {
                Node node = resource.adaptTo(Node.class);
                boolean replicationMixin = false;
                try {
                    NodeType[] mixins = node.getMixinNodeTypes();
                    for(NodeType mixin : mixins) {
                        if(mixin.getName().equals("per:Replication")) {
                            replicationMixin = true;
                            break;
                        }
                    }
                    if(!replicationMixin) {
                        NodeType nodeType = node.getPrimaryNodeType();
                        NodeType[] superTypes = nodeType.getSupertypes();
                        for(NodeType mixin : superTypes) {
                            if(mixin.getName().equals(PerConstants.PER_REPLICATION)) {
                                replicationMixin = true;
                                break;
                            }
                        }
                    }
                } catch(RepositoryException e) {
                    e.printStackTrace();
                }
                if(replicationMixin) {
                    ModifiableValueMap properties = getModifiableProperties(resource, false);
                    if(properties != null) {
                        Calendar replicated = Calendar.getInstance();
                        properties.put(PerConstants.PER_REPLICATED_BY, resourceResolver.getUserID());
                        properties.put(PerConstants.PER_REPLICATED, replicated);
                        properties.put(PerConstants.PER_REPLICATION_REF, kind + "://" + path);
                    } else {
                        log.error("Could not obtain modifiable properties from resource: '{}'", resource);
                    }
                }
                resourceResolver.commit();
            }
        } catch(LoginException e) {
            log.warn("Failed to set Replication Properties on Resource: " + path + " due to login issue", e);
        } catch(PersistenceException e) {
            log.warn("Failed to set Replication Properties on Resource: " + path + " due to persisting issue", e);
        } finally {
            if(resourceResolver != null) {
                try {
                    resourceResolver.close();
                } catch(Exception e) {
                    // ignore
                }
            }
        }
    }

    private void logEvent(String message, Event event) {
        String[] propertyNames = event.getPropertyNames();
        Map<String, String> properties = new HashMap<>();
        for(String name: propertyNames) {
            if("distribution.paths".equals(name)) {
                properties.put(name, Arrays.asList((String[])event.getProperty(name)) + "");
            } else {
                properties.put(name, event.getProperty(name) + "");
            }
        }
        log.warn(message + ", Topic: '{}', Properties: '{}'", event.getTopic(), properties);
    }

    private boolean checkEventProperties(Event event, String ... expectedPairs) {
        boolean answer = true;
        for(String pair: expectedPairs) {
            String[] tokens = pair.split("=");
            if(tokens.length == 2) {
                Object value = event.getProperty(tokens[0]);
                if(value == null) {
                    log.debug("Event Property: '{}' is not found", tokens[0]);
                    answer = false;
                } else {
                    String text = value + "";
                    answer = text.equals(tokens[1]);
                    log.debug("Event Property: '{}' does not match. Found: '{}', Expected: '{}'", tokens[0], text, tokens[1]);
                }
            } else {
                log.debug("Pair: '{}' is not of 'name=value' format", pair);
            }
        }
        return answer;
    }
}
