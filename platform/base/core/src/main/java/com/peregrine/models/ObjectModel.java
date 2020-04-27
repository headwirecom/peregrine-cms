package com.peregrine.models;

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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.commons.servlets.ServletHelper;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.DIALOG_JSON;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.OBJECT_PATH;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;

@Model(
        adaptables = Resource.class,
        resourceType = OBJECT_PRIMARY_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(name = JACKSON,
          extensions = JSON)
public class ObjectModel extends AbstractComponent {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ObjectModel(Resource r) { super(r); }

    @Inject
    private String objectPath;

    @Inject
    private String name;

    public String getName() {
        return name;
    }

    @Override
    public String getComponent() {
        String cmpName = super.getComponent();
        if(cmpName.startsWith("per:-")) {
            String ret = "";
            String[] segments = objectPath.split("/");
            for(int i = 2; i < segments.length; i++) {
                if(i > 2) { ret += "-"; }
                ret += segments[i];
            }
            return ret;
        }
        return cmpName;
    }

    @JsonAnyGetter
    public Map<String, Object> getDynamicValues() {
        Map<String, Object> answer = new HashMap<>();
        Resource source = getResource();
        ValueMap properties = source.getValueMap();
        String objectDefinitionPath = properties.get(OBJECT_PATH, String.class);
        if(objectDefinitionPath != null) {
            Resource definitionDialog = source.getResourceResolver().getResource(objectDefinitionPath + SLASH + DIALOG_JSON);
            if(definitionDialog != null) {
                InputStream is = definitionDialog.adaptTo(InputStream.class);
                if(is != null) {
                    String dialog = null;
                    try {
                        dialog = ServletHelper.asString(is).toString();
                        ObjectMapper mapper = new ObjectMapper();
                        DialogBean dialogBean = mapper.readValue(dialog, DialogBean.class);
                        for(GroupItem group: dialogBean.getGroups()) {
                            for (FieldItem field : group.getFields()) {
                                answer.put(field.getModel(), properties.get(field.getModel(), String.class));
                            }
                        }
                        for (FieldItem field : dialogBean.getFields()) {
                            answer.put(field.getModel(), properties.get(field.getModel(), String.class));
                        }
                    } catch (IOException e) {
                        if(dialog == null) {
                            logger.warn("Dialog Resource count not be read as input stream: '{}'", objectDefinitionPath + SLASH + DIALOG_JSON);
                        } else {
                            logger.warn("Failed to parse given Dialog content into Dialog Bean: '{}'", dialog);
                        }
                    }
                }
            }
        }
        return answer;
    }

    /** Represents the entire dialog.json file **/
    private static class DialogBean {
        private List<GroupItem> groups = new ArrayList<>();
        private List<FieldItem> fields = new ArrayList<>();

        public List<GroupItem> getGroups() {
            return new ArrayList<>();
        }
        public List<FieldItem> getFields() {
            return fields;
        }
        @JsonAnySetter
        public void ignore(String key, Object value) {}
    }

    /** Optional Group Items **/
    private static class GroupItem {
        private List<FieldItem> fields = new ArrayList<>();

        public List<FieldItem> getFields() {
            return fields;
        }
        @JsonAnySetter
        public void ignore(String key, Object value) {}
    }

    /** Field Items **/
    private static class FieldItem {
        private String model;

        public String getModel() {
            return model;
        }
        @JsonAnySetter
        public void ignore(String key, Object value) {}
    }
}
