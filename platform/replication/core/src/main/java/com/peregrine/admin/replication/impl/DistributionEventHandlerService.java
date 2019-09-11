package com.peregrine.admin.replication.impl;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.admin.replication.ReplicationUtil.updateReplicationProperties;
import static com.peregrine.commons.util.PerConstants.DISTRIBUTION_SUB_SERVICE;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.loginService;
import static org.apache.sling.distribution.event.DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED;
import static org.apache.sling.distribution.event.DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static org.osgi.service.event.EventConstants.EVENT_TOPIC;

@Component(
    service = EventHandler.class,
    immediate = true,
    property = {
        SERVICE_VENDOR + EQUAL + PER_PREFIX + "Replication Event Handler",
        EVENT_TOPIC + EQUAL + AGENT_PACKAGE_DISTRIBUTED,
        EVENT_TOPIC + EQUAL + IMPORTER_PACKAGE_IMPORTED
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
    private static final String AGENT = "AGENT";
    private static final String IMPORTER = "IMPORTER";
    private static final String DISTRIBUTION_PATHS = "distribution.paths";
    private static final String DISTRIBUTION_TYPE_ADD = "distribution.type=ADD";
    private static final String DISTRIBUTION_COMPONENT_KIND_AGENT = "distribution.component.kind=AGENT";
    private static final String DISTRIBUTION_COMPONENT_KIND_IMPORTER = "distribution.component.kind=IMPORTER";
    private static final String EVENT_TOPICS = "event.topics";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void handleEvent(Event event) {
        final String topic = event.getTopic();
        String kind;
        if(AGENT_PACKAGE_DISTRIBUTED.equals(topic)) {
            // Forward Agent Event
            // Check the expected properties
            if(!checkEventProperties(event, DISTRIBUTION_TYPE_ADD, DISTRIBUTION_COMPONENT_KIND_AGENT, EVENT_TOPICS + EQUAL + AGENT_PACKAGE_DISTRIBUTED)) {
                // Ignore -> Done
                logEvent("Received unexpected Agent Event", event);
                return;
            }
            kind = AGENT;

        } else
        if(IMPORTER_PACKAGE_IMPORTED.equals(topic)) {
            // Forward Agent Event
            // Check the expected properties
            if(!checkEventProperties(event, DISTRIBUTION_TYPE_ADD, DISTRIBUTION_COMPONENT_KIND_IMPORTER, EVENT_TOPICS + EQUAL + IMPORTER_PACKAGE_IMPORTED)) {
                // Ignore -> Done
                logEvent("Received unexpected Importer Event", event);
                return;
            }
            kind = IMPORTER;
        } else {
            log.trace("Received unhandled event: '{}'", event);
            return;
        }
        Object value = event.getProperty(DISTRIBUTION_PATHS);
        if(value instanceof String[]) {
            String[] paths = (String[]) value;
            for(String path: paths) {
                log.trace("Set Replication Properties on: '{}'", path);
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
            log.trace("Resource Resolver: '{}'", resourceResolver);
            Resource resource = getResource(resourceResolver, path);
            log.trace("Resource for Path: '{}': '{}'", path, resource);
            updateReplicationProperties(resource, kind + "://" + path, null);
            resourceResolver.commit();
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
            if(DISTRIBUTION_PATHS.equals(name)) {
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
            String[] tokens = pair.split(EQUAL);
            if(tokens.length == 2) {
                Object value = event.getProperty(tokens[0]);
                if(value == null) {
                    log.trace("Event Property: '{}' is not found", tokens[0]);
                    answer = false;
                } else {
                    String text = value + "";
                    answer = text.equals(tokens[1]);
                    log.trace("Event Property: '{}' does not match. Found: '{}', Expected: '{}'", tokens[0], text, tokens[1]);
                }
            } else {
                log.trace("Pair: '{}' is not of 'name=value' format", pair);
            }
        }
        return answer;
    }
}
