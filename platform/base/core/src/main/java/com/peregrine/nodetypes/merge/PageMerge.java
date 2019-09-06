package com.peregrine.nodetypes.merge;

/*-
 * #%L
 * peregrine default node types - Core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.commons.util.BindingsUseUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;

/**
 * Created by rr on 5/8/2017.
 */
public class PageMerge implements Use {

    public static final String FROM_TEMPLATE = "fromTemplate";
    public static final String TEMPLATE = "template";
    public static final String CONTENT_TEMPLATES = "/content/templates/";

    private static final ThreadLocal<RenderContext> renderContext = new ThreadLocal<>();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ModelFactory modelFactory;

    private Resource resource;

    private ResourceResolver resourceResolver;

    public static RenderContext getRenderContext() {
        return renderContext.get();
    }

    @Override
    public void init(final Bindings bindings) {
        final SlingHttpServletRequest request = BindingsUseUtil.getRequest(bindings);
        renderContext.set(new RenderContext(request));
        resource = request.getResource();
        resourceResolver = request.getResourceResolver();
        final SlingScriptHelper sling = BindingsUseUtil.getSling(bindings);
        modelFactory = sling.getService(ModelFactory.class);
    }

    public String getMerged() {
        log.debug("merge on {}", resource.getPath());
        if (JCR_CONTENT.equals(resource.getName())) {
            return toJSON(resource.getParent());
        }

        return toJSON(resource);
    }

    public String getMergedForScript() {
        return StringUtils.replace(getMerged(), "</script>", "<\\/script>");
    }

    private String toJSON(final Resource page) {
        return toJSON(findTemplateAndMergeWithPageProperties(page));
    }

    private String toJSON(final Map template) {
        final ObjectMapper mapper = new ObjectMapper();
        try (final StringWriter writer = new StringWriter()) {
            mapper.writeValue(writer, template);
            return writer.toString();
        } catch (final IOException e) {
            log.error("not able to create string writer", e);
        }

        return StringUtils.EMPTY;
    }

    private Map findTemplateAndMergeWithPageProperties(final Resource page) {
        log.debug("getMerged({})", page.getPath());
        try {
            final Resource content = page.getChild(JCR_CONTENT);
            if (content == null) {
            	return emptyMap();
            }

            final Map properties = modelFactory.exportModelForResource(
                    content,
                    JACKSON,
                    Map.class,
                    emptyMap());
            return findTemplateAndMergeWithPageProperties(page, properties);
        } catch (final ExportException e) {
            log.error("not able to export model", e);
        } catch (final MissingExporterException e) {
            log.error("not able to find exporter for model", e);
        }

        return emptyMap();
    }

    private Map<String, String> emptyMap() {
        return Collections.emptyMap();
    }

    private Map findTemplateAndMergeWithPageProperties(final Resource page, final Map properties) {
        String templatePath = (String) properties.get(TEMPLATE);
        if (StringUtils.isBlank(templatePath)) {
            templatePath = Optional.of(page)
                    .map(Resource::getParent)
                    .filter(parent -> PAGE_PRIMARY_TYPE.equals(parent.getResourceType()))
                    .map(Resource::getPath)
                    .filter(path -> path.startsWith(CONTENT_TEMPLATES))
                    .orElse(null);
        }

        if (StringUtils.isNotBlank(templatePath)) {
            final Resource template = resourceResolver.getResource(templatePath);
            final Map templateProperties = findTemplateAndMergeWithPageProperties(template);
            flagFromTemplate(templateProperties);
            return merge(templateProperties, properties);
        }

        return properties;
    }

    private void flagFromTemplate(final Map properties) {
        properties.put(FROM_TEMPLATE, Boolean.TRUE);
        properties.values().stream()
                .filter(value -> value instanceof Collection)
                .forEach(value -> flagFromTemplate((Collection) value));
    }

    private void flagFromTemplate(final Collection collection) {
        collection.stream()
                .filter(item -> item instanceof Map)
                .forEach(item -> flagFromTemplate((Map)item));
    }

    private TreeMap merge(final Map target, final Map source) {
        final TreeMap result = new TreeMap(target);
        final Set<Map.Entry> entrySet = source.entrySet();
        for (Map.Entry entry: entrySet) {
            merge(result, entry);
        }

        return result;
    }

    private void merge(final Map target, final Map.Entry source) {
        final Object key = source.getKey();
        log.debug("key is {}", key);
        final Object value = source.getValue();
        log.debug("value is {}", value == null ? null : value.getClass());
        if (COMPONENT.equals(key) && NT_UNSTRUCTURED.equals(value)) {
            return;
        }

        if (value instanceof List && target.containsKey(key)) {
            final Object targetValue = target.get(key);
            if (targetValue instanceof List) {
                merge((List) targetValue, (List) value);
            }
        } else if(!(value instanceof Map)) {
            target.put(key, value);
        }
    }

    private void merge(final List target, final List source) {
        for (final Object srcItem : source) {
            log.debug("array merge: {}", srcItem.getClass());
            boolean merged = false;
            if (srcItem instanceof Map) {
                merged = merge(target, (Map)srcItem);
            }

            if (!merged && !target.contains(srcItem)) {
                target.add(srcItem);
            }
        }
    }

    private boolean merge(final List target, final Map source) {
        final String path = (String) source.get(PATH);
        if (StringUtils.isBlank(path)) {
            return false;
        }

        boolean result = false;
        log.debug("find entry for {}", path);
        for (int i = 0; i < target.size(); i++) {
            final Map targetMap = (Map) (target.get(i));
            if (targetMap.get(PATH).equals(path)) {
                log.debug("found");
                final Map merged = merge(targetMap, source);
                target.set(i, merged);
                log.debug("{}", merged);
                result = true;
            }
        }

        return result;
    }
}
