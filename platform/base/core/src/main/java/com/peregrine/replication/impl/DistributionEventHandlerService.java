package com.peregrine.replication.impl;

import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.DistributionEventPojo;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
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

import static com.peregrine.replication.ReplicationUtil.markAsActivated;
import static com.peregrine.replication.ReplicationUtil.markAsDeactivated;
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

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void handleEvent(final Event event) {
        if (StringUtils.equalsAny(event.getTopic(), AGENT_PACKAGE_DISTRIBUTED, IMPORTER_PACKAGE_IMPORTED)) {
            setReplicationProperties(new DistributionEventPojo(event));
        }
    }

    private void setReplicationProperties(final DistributionEventPojo data) {
        try (final ResourceResolver resourceResolver = loginService(resourceResolverFactory, DISTRIBUTION_SUB_SERVICE)) {
            Arrays.stream(data.getPaths())
                    .filter(path -> PerUtil.isJcrContent(path) || !path.contains(JCR_CONTENT))
                    .forEach(path -> {
                        log.info("properties for {} were updated by dist event handler.", path);
                        final Resource resource = resourceResolver.getResource(path);
                        if (DISTRIBUTION_TYPE_ADD.equals(data.getDistributionType().name())) {
                            final String replicationRef = data.getDistributionComponentKind() + "://" + path;
                            markAsActivated(resource, replicationRef);
                        } else {
                            markAsDeactivated(resource);
                        }
                    });
        } catch (LoginException e) {
            log.error("Failed to update per:Replication properties", e);
        }
    }

}
