package com.peregrine.admin.data.impl;

import com.peregrine.admin.data.PerAsset;
import com.peregrine.admin.util.JcrUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
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
import static com.peregrine.admin.util.JcrUtil.JCR_PRIMARY_TYPE;
import static com.peregrine.admin.util.JcrUtil.METADATA;
import static com.peregrine.admin.util.JcrUtil.RENDITIONS;
import static com.peregrine.admin.util.JcrUtil.SLING_FOLDER;

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

    public void addTag(String category, String tag, Object value)
        throws PersistenceException, RepositoryException
    {
        Session session = adaptTo(Session.class);
        Resource categoryResource = getCategoryResource(category, true);
        ModifiableValueMap properties = categoryResource.adaptTo(ModifiableValueMap.class);
        if(properties != null) {
            properties.put(adjustName(tag), value);
        }
        session.save();
    }

    public Map<String, Map<String, Object>> getTags() {
        Map<String, Map<String, Object>> answer = new HashMap<>();
        Resource metadata = null;
        try {
            metadata = getOrCreateMetaData();
        } catch(PersistenceException e) {
            // Ignore
        }
        if(metadata != null) {
            for(Resource category: metadata.getChildren()) {
                String categoryName = category.getName();
                Map<String, Object> tags = getTags(category);
                answer.put(categoryName, tags);
            }
        }
        return answer;
    }

    public Map<String, Object> getTags(String category) {
        Map<String, Object> answer = new HashMap<>();
        Resource categoryResource = null;
        try {
            categoryResource = getCategoryResource(category, false);
        } catch(PersistenceException e) {
            // Ignore
        }
        if(categoryResource != null) {
            answer = getTags(categoryResource);
        }
        return answer;
    }

    private Map<String, Object> getTags(Resource category) {
        Map<String, Object> answer = new HashMap<>();
        ValueMap properties = category.getValueMap();
        for(String key: properties.keySet()) {
            if(!key.startsWith("jcr:")) {
                answer.put(key, properties.get(key));
            }
        }
        return answer;
    }

    public Object getTag(String category, String tag) {
        Object answer = null;
        Resource categoryResource = null;
        try {
            categoryResource = getCategoryResource(category, false);
            if(categoryResource != null) {
                ValueMap properties = categoryResource.getValueMap();
                answer = properties.get(adjustName(tag));
            }
        } catch(PersistenceException e) {
            // Ignore
        }
        return answer;
    }

    private Resource getOrCreateRenditionsFolder()
        throws PersistenceException
    {
        ResourceResolver resourceResolver = adaptTo(ResourceResolver.class);
        Resource renditions = getRenditions();
        if(renditions == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            renditions = resourceResolver.create(getResource(), JcrUtil.RENDITIONS, properties);
        }
        return renditions;
    }

    private Resource getRenditions() {
        return getResource().getChild(RENDITIONS);
    }

    private Resource getCategoryResource(String category, boolean create)
        throws PersistenceException
    {
        String adjustedCategory = adjustName(category);
        ResourceResolver resourceResolver = adaptTo(ResourceResolver.class);
        Resource metadata = getOrCreateMetaData();
        Resource answer = metadata.getChild(adjustedCategory);
        if(create && answer == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            answer = resourceResolver.create(metadata, adjustedCategory, properties);
        }
        return answer;
    }

    private Resource getOrCreateMetaData() throws PersistenceException {
        Resource contentResource = getContentResource();
        Resource metadata = contentResource.getChild(METADATA);
        if(metadata == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            metadata = getResource().getResourceResolver().create(getContentResource(), METADATA, properties);

        }
        return metadata;
    }

    private String adjustName(String name) {
        return name == null ?
            null :
            name.toLowerCase().replaceAll(" ", "_").replaceAll("/", "_");
    }
}
