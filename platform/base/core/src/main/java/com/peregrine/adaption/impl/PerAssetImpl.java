package com.peregrine.adaption.impl;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import com.peregrine.adaption.PerAsset;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.peregrine.assets.AssetConstants.NN_PER_DATA;
import static com.peregrine.assets.AssetConstants.PN_HEIGHT;
import static com.peregrine.assets.AssetConstants.PN_WIDTH;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerUtil.METADATA;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Peregrine Asset Wrapper Object
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
public class PerAssetImpl
    extends PerBaseImpl
    implements PerAsset
{

    public static final String JCR = "jcr:";

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
            return properties.get(JCR_DATA, InputStream.class);
        } else {
            return null;
        }
    }

    @Override
    public Iterable<Resource> listRenditions() {
        Resource renditions = null;
        try {
            renditions = getRenditionsResource(false);
        } catch(PersistenceException e) {
            // Ignore
        }
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
        Resource renditions = getRenditionsResource(true);
        Node renditionsNode = renditions.adaptTo(Node.class);
        Node renditionNode = renditionsNode.addNode(renditionName, NT_FILE);
        Node jcrContent = renditionNode.addNode(JCR_CONTENT, NT_RESOURCE);
        Binary data = session.getValueFactory().createBinary(dataStream);
        jcrContent.setProperty(JCR_DATA, data);
        jcrContent.setProperty(JCR_MIME_TYPE, mimeType);
    }

    @Override
    public void addTag(String category, String tag, Object value)
        throws PersistenceException, RepositoryException
    {
        if(value != null) {
            Resource categoryResource = getCategoryResource(category, true);
            ModifiableValueMap properties = categoryResource.adaptTo(ModifiableValueMap.class);
            if(properties != null) {
                properties.put(PerUtil.adjustMetadataName(tag), value);
            }
        }
    }

    @Override
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

    @Override
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

    /**
     * Obtains all tags of a given category
     * @param category Category resource
     * @return Map of all tags except 'jcr:' of the given category. It is empty if none found or category is null.
     */
    private Map<String, Object> getTags(Resource category) {
        Map<String, Object> answer = new HashMap<>();
        if(category != null) {
            ValueMap properties = category.getValueMap();
            for(String key : properties.keySet()) {
                if(!key.startsWith(JCR)) {
                    answer.put(key, properties.get(key));
                }
            }
        }
        return answer;
    }

    @Override
    public Object getTag(String category, String tag) {
        return getTag(category, tag, Object.class);
    }

    private <X> X getTag(String category, String tag, Class<? extends X> type) {
        try {
            Resource categoryResource = getCategoryResource(category, false);
            if(categoryResource != null) {
                ValueMap properties = categoryResource.getValueMap();
                return properties.get(PerUtil.adjustMetadataName(tag), type);
            }
        } catch(PersistenceException e) {
            // Ignore
        }

        return null;
    }

    /**
     * Obtains the Renditions from the Image
     * @param create If true the renditions will be created if not found
     * @return The Renditions resource if found or created otherwise null
     * @throws PersistenceException If resource could not be created
     */
    private Resource getRenditionsResource(boolean create)
        throws PersistenceException
    {
        ResourceResolver resourceResolver = adaptTo(ResourceResolver.class);
        Resource renditions = getResource().getChild(RENDITIONS);
        if(create && renditions == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            renditions = resourceResolver.create(getResource(), RENDITIONS, properties);
        }
        return renditions;
    }

    /**
     * Obtains the given Category from the Image's Metadata
     * @param category Name of the category
     * @param create If true the category will be created if not found
     * @return The Category resource if found or created otherwise null
     * @throws PersistenceException If resource could not be created
     */
    private Resource getCategoryResource(String category, boolean create)
        throws PersistenceException
    {
        String adjustedCategory = PerUtil.adjustMetadataName(category);
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

    /** @return Obtains the metadata resource folder from the content and if not found then create it first **/
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

    public void setDimension() throws RepositoryException, IOException {
        final InputStream is = getRenditionStream((String) null);
        // Ignore images that do not have a jcr:data element aka stream
        if (isNull(is)) {
            return;
        }

        if (endsWithIgnoreCase(getName(), ".svg")) {
            setSVGDimension(is);
            return;
        }

        setImageDimension(is);
    }

    private void setSVGDimension(final InputStream is) throws RepositoryException, PersistenceException {
        final var header = getSVGHeader(is);
        if (isBlank(header)) {
            return;
        }

        final var widthProp = normalizeSVGAlphanumeric(extractSVGProperty(header, "width"));
        final var heightProp = normalizeSVGAlphanumeric(extractSVGProperty(header, "height"));
        final var viewBoxProp = extractSVGProperty(header, "viewBox");
        final var viewBox = parseRectangle(viewBoxProp);
        if (!(isExtendedAlphanumeric(widthProp) && isExtendedAlphanumeric(heightProp)) && isNull(viewBox)) {
            return;
        }

        final int width = isExtendedAlphanumeric(widthProp)
                ? Integer.parseInt(widthProp)
                : (int) (viewBox.getWidth() - viewBox.getX());
        final int height = isExtendedAlphanumeric(heightProp)
                ? Integer.parseInt(heightProp)
                : (int) (viewBox.getHeight() - viewBox.getY());
        addDimensionTags(width, height);
    }

    private String getSVGHeader(final InputStream is) {
        final StringBuilder result = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        char c = '\0';
        while (!containsIgnoreCase(result, "<svg") || c != '>') {
            try {
                final int i = reader.read();
                if (i == -1) {
                    return null;
                }

                c = (char) i;
                result.append(c);
            } catch (final IOException e) {
                return null;
            }
        }

        return "<svg" + substringAfter(result.toString(), "<svg");
    }

    private String extractSVGProperty(final String header, final String name) {
        var result = trim(header);
        if (!contains(removeWhitespaces(result), name + "=\"")) {
            return null;
        }

        while (!startsWith(removeWhitespaces(result), "=\"")) {
            result = trim(substringAfter(result, name));
        }

        result = trim(substringAfter(result, "="));
        result = substringAfter(result, "\"");
        return substringBefore(result, "\"");
    }

    private String removeWhitespaces(final String string) {
        return replaceAll(string, "\\s", "");
    }

    private Rectangle parseRectangle(final String string) {
        if (isBlank(string)) {
            return null;
        }

        final var normalized = replaceAll(string, "\\s+", " ");
        final var parts = Arrays.stream(split(normalized, " "))
                .filter(this::isExtendedAlphanumeric)
                .map(this::normalizeSVGAlphanumeric)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (parts.size() != 4) {
            return null;
        }

        return new Rectangle(
                parts.remove(0),
                parts.remove(0),
                parts.remove(0),
                parts.remove(0)
        );
    }

    private String normalizeSVGAlphanumeric(final String string) {
        var result = lowerCase(string);
        result = substringBeforeLast(result, "pt");
        return substringBeforeLast(result, ".");
    }

    private boolean isExtendedAlphanumeric(final String string) {
        if (startsWith(string, "-")) {
            return isAlphanumeric(substringAfter(string, "-"));
        }

        return isAlphanumeric(string);
    }

    private void setImageDimension(final InputStream is) throws IOException, RepositoryException {
        final ImageInputStream iis = ImageIO.createImageInputStream(is);
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (readers.hasNext()) {
            final ImageReader reader = readers.next();
            reader.setInput(iis);
            final int minIndex = reader.getMinIndex();
            int width = reader.getWidth(minIndex);
            int height = reader.getHeight(minIndex);
            addDimensionTags(width, height);
        }
    }

    private void addDimensionTags(final int width, final int height) throws PersistenceException, RepositoryException {
        addTag(NN_PER_DATA, PN_WIDTH, width);
        addTag(NN_PER_DATA, PN_HEIGHT, height);
    }

    public Dimension getDimension() {
        final Integer width = getTag(NN_PER_DATA, PN_WIDTH, Integer.class);
        final Integer height = getTag(NN_PER_DATA, PN_HEIGHT, Integer.class);
        if (nonNull(width) && nonNull(height)) {
            return new Dimension(width, height);
        }

        return null;
    }
}
