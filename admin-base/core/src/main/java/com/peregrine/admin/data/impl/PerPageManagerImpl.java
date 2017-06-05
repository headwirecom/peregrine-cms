package com.peregrine.admin.data.impl;

import com.peregrine.admin.data.PerPage;
import com.peregrine.admin.data.PerPageManager;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Calendar;

/**
 * Created by schaefa on 6/2/17.
 */
public class PerPageManagerImpl
    implements PerPageManager
{

    private ResourceResolver resourceResolver;

    public PerPageManagerImpl(ResourceResolver resourceResolver) {
        if(resourceResolver == null) {
            throw new IllegalArgumentException("Resource Resolver must be provided");
        }
        this.resourceResolver = resourceResolver;
    }

    public PerPageManagerImpl(Resource resource) {
        if(resource == null) {
            throw new IllegalArgumentException("Resource must be provided");
        }
        this.resourceResolver = resource.getResourceResolver();
    }

    @Override
    public PerPage getPage(String pagePath) {
        PerPage page = null;
        Resource resource = resourceResolver.getResource(pagePath);
        if(resource != null) {
            page = resource.adaptTo(PerPage.class);
        }
        return page;
    }

    @Override
    public void touch(PerPage page, boolean shallow, Calendar now, boolean clearReplication) {
        if(page != null && page.isValid() && page.hasContent()) {
            ModifiableValueMap properties = page.getModifiableProperties();
            if(now != null) {
                properties.put("jcr:lastModified", now);
            }
            if(clearReplication) {
                properties.remove("per:Replicated");
                properties.remove("per:ReplicatedBy");
                properties.remove("per:ReplicationRef");
            }
            try {
                resourceResolver.commit();
            } catch(PersistenceException e) {
                //AS TODO: Log exception
            }
            if(!shallow) {
                for(PerPage child : page.listChildren()) {
                    touch(child, shallow, now, clearReplication);
                }
            }
        }
    }
}
