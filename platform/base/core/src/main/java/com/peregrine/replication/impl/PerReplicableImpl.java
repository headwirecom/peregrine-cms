package com.peregrine.replication.impl;

import com.peregrine.adaption.impl.PerBaseImpl;
import com.peregrine.replication.PerReplicable;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;
import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.*;

public class PerReplicableImpl extends PerBaseImpl implements PerReplicable {

    private final ValueMap vm;

    public PerReplicableImpl(final Resource resource) {
        super(resource);
        vm = Optional.ofNullable(getProperties())
                .orElseGet(resource::getValueMap);
    }

    private <T> T getProperty(final String name, final Class<T> type) {
        return vm.get(name, type);
    }

    private String getStringProperty(final String name) {
        return getProperty(name, String.class);
    }

    private Calendar getCalendarProperty(String name) {
        return getProperty(name, Calendar.class);
    }

    @Override
    public Resource getMainResource() {
        return Optional.ofNullable(getContentResource()).orElseGet(this::getResource);
    }

    /**
     * Is Replicated makes an inference about the replication status based on the Per:ReplicationRef property
     * If it exists but empty; the inference is the resource was deactivated
     * If the value of Per:ReplicationRef starts with AGENT, the resource was "activated"
     * If the value of Per:ReplicationRef starts with IMPORTER, the resource was imported as a result of replication
     * @return true if the resource has been replicated
     */
    @Override
    public boolean isReplicated() {
        return Optional.ofNullable(getStringProperty(PER_REPLICATION_REF))
                .map(StringUtils::isNotBlank)
                .orElse(false);
    }

    /**
     * @return true if the resource is has per:ReplicationStatus 'activated' and content was modified after the per:Replicated date
     */
    @Override
    public boolean isStale() {
        return Optional.ofNullable(getCalendarProperty(JCR_LAST_MODIFIED))
                .map(lm -> lm.after(getReplicated()))
                .orElse(false);
    }

    /**
     * @return the date for the latest replication status
     */
    @Override
    public Calendar getReplicated() {
        return getCalendarProperty(PER_REPLICATED);
    }

    /**
     * @return A string property reference about the replication
     */
    @Override
    public String getReplicationRef() {
        return getStringProperty(PER_REPLICATION_REF);
    }

    @Override
    public String getLastReplicationAction(){
        return getStringProperty(PER_REPLICATION_LAST_ACTION);
    }

}
