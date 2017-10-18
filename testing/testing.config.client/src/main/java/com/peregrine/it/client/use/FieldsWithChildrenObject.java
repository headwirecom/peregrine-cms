package com.peregrine.it.client.use;

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
import com.peregrine.nodetypes.merge.RenderContext;
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
import java.util.Collections;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;

/**
 * Created by rr on 5/8/2017.
 */
@SuppressWarnings("serial")
public class FieldsWithChildrenObject
    implements Use {

    public static final String REQUEST = "request";
    public static final String SLING = "sling";
    private final Logger log = LoggerFactory.getLogger(FieldsWithChildrenObject.class);

    private static ThreadLocal<RenderContext> renderContext = new ThreadLocal<RenderContext>();

//    @Reference
    ModelFactory modelFactory;

    private SlingHttpServletRequest request;

    public static RenderContext getRenderContext() {
        return renderContext.get();
    }

    public String getObject() {
        Resource res = request.getResource();
        log.trace("Merge Resource: '{}'", res.getPath());
        if(res.getName().equals(JCR_CONTENT)) {
            res = res.getParent();
        }
        return toJSON(getObject(res));
    }

    public Map getObject(Resource resource) {
        log.trace("Get Merge Resource: '{}'", resource.getPath());
        Map answer = Collections.<String, String> emptyMap();
        try {
            answer = modelFactory.exportModelForResource(
                resource,
                JACKSON,
                Map.class,
                Collections.<String, String> emptyMap()
            );
        } catch (ExportException e) {
            log.error("not able to export model", e);
        } catch (MissingExporterException e) {
            log.error("not able to find exporter for model", e);
        } catch(Exception e) {
            log.error("Generic Failed to get Merged Object", e);
        }
        log.trace("Merged Map of Resource: '{}': '{}'", resource.getPath(), answer);
        return answer;
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
        request = (SlingHttpServletRequest) bindings.get(REQUEST);
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get(SLING);
        modelFactory = sling.getService(ModelFactory.class);
        renderContext.set(new RenderContext(request));
    }
}
