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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by rr on 5/8/2017.
 */
@SuppressWarnings("serial")
public class PageMerge implements Use {

    private final Logger log = LoggerFactory.getLogger(PageMerge.class);

    private static ThreadLocal<RenderContext> renderContext = new ThreadLocal<RenderContext>();

//    @Reference
    ModelFactory modelFactory;

    private SlingHttpServletRequest request;

    public static RenderContext getRenderContext() {
        return renderContext.get();
    }

    public String getMerged() {
        return toJSON(getMerged(request.getResource()));
    }

    public Map getMerged(Resource resource) {
        try {
            Map page = modelFactory.exportModelForResource(resource.getChild("jcr:content"),
                    "jackson", Map.class,
                    Collections.<String, String> emptyMap());
            String templatePath = (String) page.get("template");
            if(templatePath == null) {
                if(resource.getParent().getPath().startsWith("/content/templates/")) {
                    // only use the parent as a template of a template if it is in fact a page
                    if(resource.getParent().getResourceType().equals("per:Page")) {
                        templatePath = resource.getParent().getPath();
                    }
                }
            }
            if(templatePath != null) {
//                Map template = modelFactory.exportModelForResource(request.getResourceResolver().getResource(templatePath).getChild("jcr:content"),
//                        "jackson", Map.class,
//                        Collections.<String, String> emptyMap());
                Map template = getMerged(request.getResourceResolver().getResource(templatePath));
                flagFromTemplate(template);
                return merge(template, page);
            }
            return page;
        } catch (ExportException e) {
            log.error("not able to export model", e);
        } catch (MissingExporterException e) {
            log.error("not able to find exporter for model", e);
        }
        return Collections.<String, String> emptyMap();
    }

    private void flagFromTemplate(Map template) {
        template.put("fromTemplate", Boolean.TRUE);
        for(Object key: template.keySet()) {
            Object value = template.get(key);
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

        for (Object key: page.keySet()) {
            Object value = page.get(key);
            log.debug("key is {}", key);
            log.debug("value is {}", value == null ? value : value.getClass());
            if(key.equals("component") && value.equals("nt:unstructured")) continue;
            if(value instanceof Map) {

            } else if(value instanceof ArrayList) {
                mergeArrays((ArrayList) res.get(key), (ArrayList) value);
            } else {
                res.put(key, value);
            }
        }
//        res.putAll(page);
        return res;
    }

    private void mergeArrays(ArrayList target, ArrayList value) {
        for (Iterator it = value.iterator(); it.hasNext(); ) {
            Object val = it.next();
            log.debug("array megre: {}",val.getClass());
            boolean merged = false;
            if(val instanceof Map) {
                Map map = (Map) val;
                String path = (String) map.get("path");
                if(path != null) {
                    log.debug("find entry for {}", path);
                    for (int i = 0; i < target.size(); i++) {
                        Object t = target.get(i);
                        if(((Map)t).get("path").equals(path)) {
                            log.debug("found");
                            target.set(i, merge((Map)t, map));
                            log.debug("{}", target.get(i));
                            merged = true;
                        }
                    }
                }
            }

            if(!target.contains(val) && !merged) {
                target.add(val);
            }
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

    @Override
    public void init(Bindings bindings) {
        request = (SlingHttpServletRequest) bindings.get("request");
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get("sling");
        modelFactory = sling.getService(ModelFactory.class);
        renderContext.set(new RenderContext(request));
    }
}
