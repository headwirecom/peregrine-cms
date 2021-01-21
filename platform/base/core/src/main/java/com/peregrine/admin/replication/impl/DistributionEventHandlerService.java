package com.peregrine.admin.replication.impl;

import com.peregrine.admin.replication.DistributionEventPojo;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.peregrine.admin.replication.ReplicationUtil.updateReplicationProperties;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.loginService;
import static org.apache.sling.distribution.event.DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED;
import static org.apache.sling.distribution.event.DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static org.osgi.service.event.EventConstants.EVENT_TOPIC;

@Component(
    service = EventHandler.class,
    immediate = true,
    property = {
        SERVICE_VENDOR + EQUALS + PER_PREFIX + "Replication Event Handler",
        EVENT_TOPIC + EQUALS + AGENT_PACKAGE_DISTRIBUTED,
        EVENT_TOPIC + EQUALS + IMPORTER_PACKAGE_IMPORTED
    }
)
/**
 * This service will react to Sling Distribution Events to set the properties
 * on the replicated nodes both on the Author and Publish side
 *
 * In order for this to work there must be a Service User defined: 'defaultAgentService'
 * with write access to wherever properties are written too.
 */
public class DistributionEventHandlerService implements EventHandler {

    private static final String DISTRIBUTION_TYPE_ADD = "ADD";
    private static final String DISTRIBUTION_TYPE_DELETE = "DELETE";


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void handleEvent(Event event) {
        final String topic = event.getTopic();

        if (AGENT_PACKAGE_DISTRIBUTED.equals(topic) || IMPORTER_PACKAGE_IMPORTED.equals(topic)) {
            final DistributionEventPojo distributionEventPojo = new DistributionEventPojo(event);
            setReplicationProperties(distributionEventPojo);
        }
    }

    private void setReplicationProperties(DistributionEventPojo distributionEventPojo) {
        ResourceResolver resourceResolver = null;

        try {
            resourceResolver = loginService(resourceResolverFactory, DISTRIBUTION_SUB_SERVICE);
            ResourceResolver finalResourceResolver = resourceResolver;
            List<String> paths = Arrays.stream(distributionEventPojo.getPaths())
                    .filter( path -> path.endsWith(JCR_CONTENT) || !path.contains(JCR_CONTENT))
                    .collect(Collectors.toList());

            for (String path : paths) {
                String replicationRef = "";
                Resource resource = finalResourceResolver.getResource(path);
                if (DISTRIBUTION_TYPE_ADD.equals( distributionEventPojo.getDistributionType().name())) {
                    replicationRef = distributionEventPojo.getDistributionComponentKind() + "://" + path;

                } else if (DISTRIBUTION_TYPE_DELETE.equals( distributionEventPojo.getDistributionType().name())) {
                    replicationRef = null;
                }
                log.info("properties for {} were updated by dist event handler.",path);
                updateReplicationProperties(resource, replicationRef, null);
            }
        } catch (LoginException e) {
            log.error("Failed to update per:Replication properties", e);
        }
    }

}
