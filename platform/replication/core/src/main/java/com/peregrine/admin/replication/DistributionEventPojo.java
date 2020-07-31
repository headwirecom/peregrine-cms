package com.peregrine.admin.replication;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.distribution.DistributionRequestType;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.commons.util.PerConstants.DISTRIBUTION_SUB_SERVICE;
import static com.peregrine.commons.util.PerUtil.loginService;

public class DistributionEventPojo {

    private static final String DISTRIBUTION_PATHS = "distribution.paths";
    private static final String DISTRIBUTION_TYPE = "distribution.type";
    private static final String EVENT_TOPICS = "event.topics";
    private static final String DISTRIBUTION_COMPONENT_KIND = "distribution.component.kind";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String[] paths;
    private DistributionRequestType distributionType;
    private String eventTopic;
    private String distributionComponentKind;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public DistributionRequestType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(DistributionRequestType distributionType) {
        this.distributionType = distributionType;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    public String getDistributionComponentKind() {
        return distributionComponentKind;
    }

    public void setDistributionComponentKind(String distributionComponentKind) {
        this.distributionComponentKind = distributionComponentKind;
    }


    public DistributionEventPojo(){

    }

    public void setReplicationProperties(){
        try {
            ResourceResolver resourceResolver = loginService(resourceResolverFactory, DISTRIBUTION_SUB_SERVICE);
            for (String path : this.paths){
                logger.info(path);
            }
        } catch (LoginException e) {
            logger.error("Could not set replication properties", e);
        }
    }

    public DistributionEventPojo(Event event){
        this.eventTopic  = event.getTopic();
        this.distributionType  = (DistributionRequestType) event.getProperty(DISTRIBUTION_TYPE);
        this.paths = (String[]) event.getProperty(DISTRIBUTION_PATHS);
        this.distributionComponentKind = (String) event.getProperty(DISTRIBUTION_COMPONENT_KIND);
    }

}
