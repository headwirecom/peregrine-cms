package com.peregrine.admin.data.impl;

import com.peregrine.admin.data.PerAsset;
import com.peregrine.admin.util.JcrUtil;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.admin.util.JcrUtil.JCR_CONTENT;
import static com.peregrine.admin.util.JcrUtil.JCR_MIME_TYPE;
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
        if(name == null) {
            return getRenditionStream((Resource) null);
        }
        Iterable<Resource> renditions = listRenditions();
        for(Resource rendition: renditions) {
            if(rendition.getName().equals(name)) {
                return getRenditionStream(rendition);
            }
        }
        return null;
    }

    @Override
    public InputStream getRenditionStream(Resource resource) {
        Resource jcrContent = resource != null ?
            resource.getChild(JCR_CONTENT) :
            getContentResource();
        if(jcrContent != null) {
            ValueMap properties = jcrContent.getValueMap();
            return properties.get(JcrUtil.JCR_DATA, InputStream.class);
        } else {
            return null;
        }
    }

    @Override
    public Iterable<Resource> listRenditions() {
        Resource renditions = getRenditions();
        if(renditions != null) {
            return renditions.getChildren();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void addRendition(String renditionName, InputStream dataStream, String mimeType)
        throws PersistenceException, RepositoryException
    {
        Session session = adaptTo(Session.class);
        Resource renditions = getOrCreateRenditionsFolder();
        Node renditionsNode = renditions.adaptTo(Node.class);
        Node renditionNode = renditionsNode.addNode(renditionName, JcrUtil.NT_FILE);
        Node jcrContent = renditionNode.addNode(JcrUtil.JCR_CONTENT, JcrUtil.NT_RESOURCE);
        Binary data = session.getValueFactory().createBinary(dataStream);
        jcrContent.setProperty(JcrUtil.JCR_DATA, data);
        jcrContent.setProperty(JCR_MIME_TYPE, mimeType);
        session.save();
    }

    private Resource getOrCreateRenditionsFolder()
        throws PersistenceException
    {
        ResourceResolver resourceResolver = adaptTo(ResourceResolver.class);
        Resource renditions = getRenditions();
        if(renditions == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JcrUtil.JCR_PRIMARY_TYPE, JcrUtil.SLING_FOLDER);
            renditions = resourceResolver.create(getResource(), JcrUtil.RENDITIONS, properties);
        }
        return renditions;
    }

    private Resource getRenditions() {
        return getResource().getChild(RENDITIONS);
    }
}
