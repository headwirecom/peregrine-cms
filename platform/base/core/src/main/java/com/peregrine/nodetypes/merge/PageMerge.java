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

import static com.peregrine.commons.util.PerConstants.*;

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

    private SlingHttpServletRequest request;

    private Resource resource;

    public static RenderContext getRenderContext() {
        return renderContext.get();
    }

    @Override
    public void init(final Bindings bindings) {
        request = BindingsUseUtil.getRequest(bindings);
        renderContext.set(new RenderContext(request));
        resource = request.getResource();
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

    private String toJSON(final Resource resource) {
        return toJSON(getMerged(resource));
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

    private Map getMerged(final Resource resource) {
        log.debug("getMerged({})", resource.getPath());
        try {
            final Resource content = resource.getChild(JCR_CONTENT);
            if (content == null) {
            	return emptyMap();
            }

            final Map pageProperties = modelFactory.exportModelForResource(
                    content,
                    JACKSON,
                    Map.class,
                    emptyMap());
            return getMerged(resource, pageProperties);
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

    private Map getMerged(final Resource resource, final Map pageProperties) {
        String templatePath = (String) pageProperties.get(TEMPLATE);
        if (StringUtils.isBlank(templatePath)) {
            templatePath = Optional.of(resource)
                    .map(Resource::getParent)
                    .filter(parent -> PAGE_PRIMARY_TYPE.equals(parent.getResourceType()))
                    .map(Resource::getPath)
                    .filter(path -> path.startsWith(CONTENT_TEMPLATES))
                    .orElse(null);
        }

        if (StringUtils.isNotBlank(templatePath)) {
            final Map templateProperties = getMerged(request.getResourceResolver().getResource(templatePath));
            flagFromTemplate(templateProperties);
            return merge(templateProperties, pageProperties);
        }

        return pageProperties;
    }

    private void flagFromTemplate(final Map templateProperties) {
        templateProperties.put(FROM_TEMPLATE, Boolean.TRUE);
        templateProperties.values().stream()
                .filter(value -> value instanceof Collection)
                .forEach(value -> flagFromTemplate((Collection) value));
    }

    private void flagFromTemplate(final Collection collection) {
        collection.stream()
                .filter(item -> item instanceof Map)
                .forEach(item -> flagFromTemplate((Map)item));
    }

    private Map merge(final Map templateProperties, final Map pageProperties) {
        final TreeMap result = new TreeMap();
        result.putAll(templateProperties);
        final Set<Map.Entry> entrySet = pageProperties.entrySet();
        for (Map.Entry entry: entrySet) {
            merge(result, entry);
        }

        return result;
    }

    private void merge(final Map target, final Map.Entry entry) {
        final Object key = entry.getKey();
        log.debug("key is {}", key);
        final Object value = entry.getValue();
        log.debug("value is {}", value == null ? null : value.getClass());
        if (COMPONENT.equals(key) && NT_UNSTRUCTURED.equals(value)) {
            return;
        }

        if (value instanceof List && target.containsKey(key)) {
            final Object o = target.get(key);
            if (o instanceof List) {
                merge((List) o, (List) value);
            }
        } else if(!(value instanceof Map)) {
            target.put(key, value);
        }
    }

    private void merge(final List target, final List value) {
        for (final Object v : value) {
            log.debug("array merge: {}", v.getClass());
            boolean merged = false;
            if (v instanceof Map) {
                merged = merge(target, (Map)v);
            }

            if (!merged && !target.contains(v)) {
                target.add(v);
            }
        }
    }

    private boolean merge(final List target, final Map map) {
        final String path = (String) map.get(PATH);
        if (StringUtils.isBlank(path)) {
            return false;
        }

        boolean result = false;
        log.debug("find entry for {}", path);
        for (int i = 0; i < target.size(); i++) {
            final Map targetMap = (Map) (target.get(i));
            if (targetMap.get(PATH).equals(path)) {
                log.debug("found");
                final Map merged = merge(targetMap, map);
                target.set(i, merged);
                log.debug("{}", merged);
                result = true;
            }
        }

        return result;
    }
}
