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

import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static java.util.regex.Pattern.compile;
import static org.apache.sling.api.scripting.SlingBindings.RESOLVER;
import static org.apache.sling.api.scripting.SlingBindings.RESOURCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import javax.script.Bindings;
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

/**
 * Created by rr on 5/8/2017.
 */
@SuppressWarnings("serial")
public class PageMerge implements Use {

    private final Logger log = LoggerFactory.getLogger(PageMerge.class);

    private static ThreadLocal<RenderContext> renderContext = new ThreadLocal<>();

    public static final String FROM_TEMPLATE = "fromTemplate";
    public static final String CHILDREN = "children";
    public static final String REQUEST = "request";
    public static final String SLING = "sling";
    public static final String TEMPLATE = "template";
    public static final String MODEL_FACTORY = "modelFactory";
    public static final String REGEX_TEMPLATES = "(?<=\\/content\\/)([a-zA-Z0-9\\\\s\\\\_-])*(?=\\/templates)";

    private ModelFactory modelFactory;
    private ResourceResolver resolver;
    private SlingHttpServletRequest request;
    private Resource resource;
    public static RenderContext getRenderContext() {
        return renderContext.get();
    }

    public String getMerged() {
        log.debug("merge on {}", request.getResource().getPath());
        Resource res = request.getResource();
        if(res.getName().equals(JCR_CONTENT)) {
            res = res.getParent();
        }
        return toJSON(getMerged(res));
    }

    public List<Resource> getMergedResources(){
        List<Resource> pageResources = new ArrayList<>();
        if (Objects.isNull(resource)){
            resource = request.getResource();
        }
        if(resource.getName().equals(JCR_CONTENT)) {
            resource = resource.getParent();
        }
        Map resourceMap = getMerged(resource);
        // MAP to List
        List<Map> maps = (ArrayList) resourceMap.get(CHILDREN);
        for (Map map : maps) {
            getChildren(map, pageResources);
        }
        return pageResources;
    }

    private void getChildren(Map map, List resources){
        String templatePath = getTemplatePath();
        String relativePath = (String) map.get("path");
        String basePath = resource.getPath();
        if (Objects.nonNull(map.get(FROM_TEMPLATE)) && (boolean) map.get(FROM_TEMPLATE)){
        // if fromTemplate is set true determine whether the resource is an container empty container
            List<Map> childMaps = (ArrayList) map.get(CHILDREN);
            if (Objects.nonNull(childMaps)){
                // this represents a page editing content container
                // so basePath needs to be readjusted to the page path
                basePath = resource.getPath();
            } else {
                basePath = templatePath;
            }

            Resource templateContent = this.resolver.getResource(basePath+relativePath);
            Resource templateResource = this.resolver.getResource(templatePath);
            while (Objects.isNull(templateContent) && Objects.nonNull(templateResource)){
                // while loop logic. Enter if templateContent is null, but templateResource is non null.
                // then try to resolve the templateContent from template parents content
                // exit while loop when templateContent is resolved or templateResource becomes null
                templateResource = templateResource.getParent();
                if (Objects.nonNull(templateResource)) {
                    templateContent = resolver.getResource(templateResource.getPath()+relativePath);
                }
            }
            if(Objects.nonNull(templateContent)){
                resources.add(templateContent);
            }
        } else {
            // page content
            Resource pageResource = this.resource.getResourceResolver()
                    .getResource(resource.getPath()+relativePath);
            resources.add(pageResource);
        }
    }

    public String getMergedForScript() {
        log.debug("merge on {}", request.getResource().getPath());
        Resource res = request.getResource();
        if(res.getName().equals(JCR_CONTENT)) {
            res = res.getParent();
        }
        String merged = toJSON(getMerged(res));
        return merged.replaceAll("</script>", "<\\\\/script>");
    }

    public Map getMerged(Resource resource) {
        log.debug("getMerge({})", resource.getPath());
        try {
            Resource content = resource.getChild(JCR_CONTENT);
            if(content == null) return Collections.<String, String> emptyMap();
            Map page = modelFactory
                .exportModelForResource(content, JACKSON, Map.class, Collections.emptyMap());
            String templatePath = (String) page.get(TEMPLATE);
            if(templatePath == null) {
                if(compile(REGEX_TEMPLATES).matcher(resource.getParent().getPath()).find()) {
                    if(resource.getParent().getResourceType().equals(PAGE_PRIMARY_TYPE)) {
                        templatePath = resource.getParent().getPath();
                    }
                }
            }
            if(templatePath != null) {
                Map template = getMerged(this.resolver.getResource(templatePath));
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
        template.put(FROM_TEMPLATE, Boolean.TRUE);
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

    private String getTemplatePath(){
        Resource jcrContent = this.resource.getChild(JCR_CONTENT);
        return jcrContent.getValueMap().get(TEMPLATE, String.class);
    }

    private Map merge(Map template, Map page) {
        TreeMap res = new TreeMap();
        res.putAll(template);

        for (Object key: page.keySet()) {
            Object value = page.get(key);
            log.debug("key is {}", key);
            log.debug("value is {}", value == null ? value : value.getClass());
            if(key.equals(COMPONENT) && value.equals(NT_UNSTRUCTURED)) continue;
            if(value instanceof Map) {

            } else if(value instanceof ArrayList) {
                mergeArrays((ArrayList) res.get(key), (ArrayList) value);
            } else {
                res.put(key, value);
            }
        }
        return res;
    }

    private void mergeArrays(ArrayList target, ArrayList value) {
        for (Iterator it = value.iterator(); it.hasNext(); ) {
            Object val = it.next();
            log.debug("array merge: {}",val.getClass());
            boolean merged = false;
            if(val instanceof Map) {
                Map map = (Map) val;
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
    public void init(final Bindings bindings) {
        request = (SlingHttpServletRequest) bindings.get(REQUEST);
        resource = (Resource) bindings.get(RESOURCE);
        renderContext.set(new RenderContext(request));
        resolver = (ResourceResolver) bindings.get(RESOLVER);
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get(SLING);
        if (Objects.nonNull(sling)) {
            // the typical path
            modelFactory = sling.getService(ModelFactory.class);
        } else if ( Objects.nonNull(bindings.get(MODEL_FACTORY))) {
            // for unit testing
            modelFactory = (ModelFactory) bindings.get(MODEL_FACTORY);
        }
    }
}
