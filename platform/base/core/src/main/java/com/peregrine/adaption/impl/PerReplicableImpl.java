package com.peregrine.adaption.impl;

import com.peregrine.adaption.PerReplicable;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;

import static com.peregrine.commons.util.PerConstants.*;

public class PerReplicableImpl extends PerBaseImpl implements PerReplicable {
    private ValueMap vm;
    private ModifiableValueMap modifiableValueMap;

    public PerReplicableImpl(Resource resource) {
        super(resource);
        vm = this.getProperties();
        if (vm == null) {
            vm = this.getResource().getValueMap();
        }
        modifiableValueMap = this.getModifiableProperties();
        if (modifiableValueMap == null) {
            modifiableValueMap = this.getResource().adaptTo(ModifiableValueMap.class);
        }
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
        String replicatedRef = vm.get(PER_REPLICATION_REF, String.class);
        return replicatedRef != null && !replicatedRef.isEmpty() ;
    }

    /**
     * @return true if the resource is has per:ReplicationStatus 'activated' and content was modified after the per:Replicated date
     */
    @Override
    public boolean isStale() {
        if (this.vm.get(JCR_LAST_MODIFIED, Calendar.class) != null){
            return this.vm.get(JCR_LAST_MODIFIED, Calendar.class).after(getReplicated());
        }
        return false;
    }

    /**
     * @return the date for the latest replication status
     */
    @Override
    public Calendar getReplicated() {
        return this.vm.get(PER_REPLICATED, Calendar.class);
    }

    /**
     * @return A string property reference about the replication
     */
    @Override
    public String getReplicationRef() {
        return this.vm.get(PER_REPLICATION_REF, String.class);
    }


    @Override
    public void setLastReplicationActionAsActivated() {
        writeStringProperty(PER_REPLICATION_LAST_ACTION, ACTIVATED);
    }

    @Override
    public void setLastReplicationActionAsDeactivated(){
        writeStringProperty(PER_REPLICATION_LAST_ACTION, DEACTIVATED);
    }

    @Override
    public String getLastReplicationAction(){
        return this.vm.get(PER_REPLICATION_LAST_ACTION, String.class);
    }

    private void writeStringProperty(String name, String value){
        this.modifiableValueMap.put(name, value);
        try {
            this.getResource().getResourceResolver().commit();
        } catch (PersistenceException e) {
            logger.error("Could not set replication status to pending", e);
        }
    }
}
