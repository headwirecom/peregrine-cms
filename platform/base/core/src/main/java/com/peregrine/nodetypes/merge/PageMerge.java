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

    public static RenderContext getRenderContext() {
        return renderContext.get();
    }

    @Override
    public void init(final Bindings bindings) {
        request = BindingsUseUtil.getRequest(bindings);
        renderContext.set(new RenderContext(request));
        final SlingScriptHelper sling = BindingsUseUtil.getSling(bindings);
        modelFactory = sling.getService(ModelFactory.class);
    }

    public String getMerged() {
        Resource res = request.getResource();
        log.debug("merge on {}", res.getPath());
        if(JCR_CONTENT.equals(res.getName())) {
            res = res.getParent();
        }
        return toJSON(getMerged(res));
    }

    public String getMergedForScript() {
        return getMerged().replace("</script>", "<\\/script>");
    }

    private Map getMerged(Resource resource) {
        log.debug("getMerge({})", resource.getPath());
        try {
            Resource content = resource.getChild(JCR_CONTENT);
            if(content == null) {
            	return Collections.<String, String> emptyMap();
            }

            Map page = modelFactory.exportModelForResource(content,
                    JACKSON, Map.class,
                    Collections.emptyMap());
            return getMerged(resource, page);
        } catch (ExportException e) {
            log.error("not able to export model", e);
        } catch (MissingExporterException e) {
            log.error("not able to find exporter for model", e);
        }
        return Collections.<String, String> emptyMap();
    }

    private Map getMerged(Resource resource, Map page) {
        String templatePath = (String) page.get(TEMPLATE);
        if(StringUtils.isBlank(templatePath)) {
            templatePath = Optional.of(resource)
                    .map(Resource::getParent)
                    .filter(parent -> parent.getResourceType().equals(PAGE_PRIMARY_TYPE))
                    .map(Resource::getPath)
                    .filter(path -> path.startsWith(CONTENT_TEMPLATES))
                    .orElse(null);
        }
        if(StringUtils.isNotBlank(templatePath)) {
            final Map template = getMerged(request.getResourceResolver().getResource(templatePath));
            flagFromTemplate(template);
            return merge(template, page);
        }
        return page;
    }

    private void flagFromTemplate(Map template) {
        template.put(FROM_TEMPLATE, Boolean.TRUE);
        for(final Object value: template.values()) {
            if(value instanceof ArrayList) {
                ArrayList arr = (ArrayList) value;
                for(int i = 0; i < arr.size(); i++) {
                    Object item = arr.get(i);
                    if(item instanceof Map) {
                        flagFromTemplate((Map)arr.get(i));
                    }
                }
            }
        }
    }

    private Map merge(Map template, Map page) {
        TreeMap res = new TreeMap();
        res.putAll(template);

        final Set<Map.Entry> entrySet = page.entrySet();
        for(Map.Entry entry: entrySet) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            log.debug("key is {}", key);
            log.debug("value is {}", value == null ? null : value.getClass());
            if(COMPONENT.equals(key) && NT_UNSTRUCTURED.equals(value)) {
                continue;
            }
            if(value instanceof ArrayList) {
                merge((ArrayList) res.get(key), (ArrayList) value);
            } else if(!(value instanceof Map)) {
                res.put(key, value);
            }
        }
        return res;
    }

    private void merge(ArrayList target, ArrayList value) {
        for (Iterator it = value.iterator(); it.hasNext(); ) {
            final Object next = it.next();
            log.debug("array merge: {}", next.getClass());
            merge(target, next);
        }
    }

    private void merge(ArrayList target, Object value) {
        boolean merged = false;
        if(value instanceof Map) {
            Map map = (Map) value;
            String path = (String) map.get(PATH);
            if(path != null) {
                log.debug("find entry for {}", path);
                for (int i = 0; i < target.size(); i++) {
                    Object t = target.get(i);
                    if(((Map)t).get(PATH).equals(path)) {
                        log.debug("found");
                        target.set(i, merge((Map)t, map));
                        log.debug("{}", target.get(i));
                        merged = true;
                    }
                }
            }
        }

        if(!target.contains(value) && !merged) {
            target.add(value);
        }
    }

    private String toJSON(Map template) {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer, template);
            writer.close();
        } catch (IOException e) {
            log.error("not able to create string writer", e);
        }
        return writer.toString();
    }
}
