package com.peregrine.admin.data.impl;

import com.peregrine.admin.data.PerAsset;
import com.peregrine.admin.util.JcrUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.io.InputStream;

import static com.peregrine.admin.util.JcrUtil.RENDITIONS;

/**
 * Created by schaefa on 6/4/17.
 */
public class PerAssetImpl
    extends PerBaseImpl
    implements PerAsset
{
    public PerAssetImpl(Resource resource) {
        super(resource);
    }

    @Override
    public InputStream getRenditionStream(String name) {
        Iterable<Resource> renditions = listRenditions();
        for(Resource rendition: renditions) {
            if(rendition.getName().equals(name)) {
                getRenditionStream(rendition);
            }
        }
        return null;
    }

    @Override
    public InputStream getRenditionStream(Resource resource) {
        ValueMap properties = resource != null ? resource.getValueMap() : null;
        return properties != null ?
            properties.get(JcrUtil.JCR_DATA, InputStream.class) :
            null;
    }

    @Override
    public Iterable<Resource> listRenditions() {
        Resource renditions = getRenditions();
        if(renditions != null) {
            return renditions.getChildren();
        }
        return null;
    }

    private Resource getRenditions() {
        Resource content = getContentResource();
        if(content != null) {
            return content.getChild(RENDITIONS);
        }
        return null;
    }
}
