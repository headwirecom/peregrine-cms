package com.peregrine.admin.replication;

import org.apache.sling.distribution.DistributionRequestType;
import org.osgi.service.event.Event;

public class DistributionEventPojo {

    private static final String DISTRIBUTION_PATHS = "distribution.paths";
    private static final String DISTRIBUTION_TYPE = "distribution.type";
    private static final String DISTRIBUTION_COMPONENT_KIND = "distribution.component.kind";
    private String[] paths;
    private DistributionRequestType distributionType;
    private String eventTopic;
    private String distributionComponentKind;

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

    public DistributionEventPojo(Event event){
        this.eventTopic  = event.getTopic();
        this.distributionType  = (DistributionRequestType) event.getProperty(DISTRIBUTION_TYPE);
        this.paths = (String[]) event.getProperty(DISTRIBUTION_PATHS);
        this.distributionComponentKind = (String) event.getProperty(DISTRIBUTION_COMPONENT_KIND);
    }

}
