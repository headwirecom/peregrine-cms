package com.peregrine.admin.replication;

import org.apache.sling.api.resource.Resource;

import java.util.Date;

public class ReferenceReplicationStatus {

    private Resource resource = null;
    private boolean activated = false;
    private Date activatedDate = null;
    private Date lastModifiedDate = null;

    public ReferenceReplicationStatus(){

    }

    public ReferenceReplicationStatus(Resource resource){
        this.resource = resource;

    }

    public Date getActivatedDate() {
        return activatedDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public boolean isActivated() {
        return activated;
    }

}
