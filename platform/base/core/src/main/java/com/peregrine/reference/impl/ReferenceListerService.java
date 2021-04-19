package com.peregrine.reference.impl;

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

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.listMissingParents;
import static java.util.Objects.isNull;

import com.peregrine.commons.util.PerUtil;
import com.peregrine.commons.util.PerUtil.MissingOrOutdatedResourceChecker;
import com.peregrine.reference.Reference;
import com.peregrine.reference.ReferenceLister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists References from and to a given Page
 *
 * Created by Andreas Schaefer on 5/25/17.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    service = ReferenceLister.class,
    immediate = true
)
@Designate(ocd = ReferenceListerService.Configuration.class)
public final class ReferenceListerService implements ReferenceLister {

    @ObjectClassDefinition(
        name = "Peregrine: Reference List Provider",
        description = "Provides a list of referenced resources for a given resource"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "ReferencePrefix",
            description = "List of Prefixes to find references in a resource"
        )
        String[] referencePrefix() default "/content/";
        @AttributeDefinition(
            name = "ReferencedByRoot",
            description = "List of Roots to look for the referenced by resources"
        )
        String[] referencedByRoot() default "/content";
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private List<String> referencePrefixList = new ArrayList<>();
    private List<String> referencedByRootList = new ArrayList<>();

    @Override
    public List<Resource> getReferenceList(boolean transitive, Resource resource, boolean deep) {
        return getReferenceList(transitive, resource, deep, PerUtil.ADD_ALL_RESOURCE_CHECKER);
    }

    @Override
    public List<Resource> getReferenceList(boolean transitive, Resource resource, boolean deep, PerUtil.ResourceChecker checker) {
        return getReferenceList(transitive, resource, deep, null, null, checker);
    }

    @Override
    public List<Resource> getReferenceList(boolean transitive, Resource resource, boolean deep, Resource source, Resource target) {
        return getReferenceList(transitive, resource, deep, source, target, PerUtil.ADD_ALL_RESOURCE_CHECKER);
    }

    @Override
    public List<Resource> getReferenceList(boolean transitive, Resource resource, boolean deep, Resource source, Resource target, PerUtil.ResourceChecker checker) {
        List<Resource> answer = new ArrayList<>();
        TraversingContext context = new TraversingContext(checker).setTransitive(transitive).setDeep(deep);
        checkResource(resource, context, answer, source, target);
        return answer;
    }

    @Override
    public List<Reference> getReferencedByList(final Resource resource) {
        if (isNull(resource)) {
            return Collections.emptyList();
        }

        final List<Reference> answer = new ArrayList<>();
        final ResourceResolver resourceResolver = resource.getResourceResolver();
        final String path = resource.getPath();
        referencedByRootList.stream()
                .map(resourceResolver::getResource)
                .filter(Objects::nonNull)
                .forEach(r -> traverseTreeReverse(r, path, answer));
        return answer;
    }

    /**
     * Check the given Resource if it has a reference
     *
     * @param resource Resource to check for references
     * @param context Traversing Context telling if this is transitive, deep traverse and if the paths are allowed (for non-deep copy) and if they are not already visited
     * @param response List containing the found references. It will contain any given resource only once
     * @param source Optional root resource of the source. If source and target is provided then missing parents are added as well
     * @param target Optional root resource of the target
     */
    private void checkResource(Resource resource, TraversingContext context, List<Resource> response, Resource source, Resource target) {
        if(resource != null && context.doAdd(resource)) {
            parseProperties(resource, context, response, source, target);
            if(!context.doAddChildren(resource)) {
                return;
            }

            Resource jcrContent = resource.getChild(JCR_CONTENT);
            if(!context.isDeep()) {
                if(jcrContent != null) {
                    context.addDeepLimit(jcrContent.getPath());
                    parseProperties(jcrContent, context, response, source, target);
                    // Loop of all its children
                    traverseTree(jcrContent, context, response, source, target);
                }
            } else {
                Iterable<Resource> children = resource.getChildren();
                for(Resource child : children) {
                    parseProperties(child, context, response, source, target);
                    // Loop of all its children
                    traverseTree(child, context, response, source, target);
                }
            }
        }
    }

    /**
     * Go through the given resources children and check their properties and their children
     * @param resource Parent resource (call is ignored if null)
     * @param context Traversing context
     * @param response List containing the found references. It will contain any given resource only once
     * @param source Optional root resource of the source. If source and target is provided then missing parents are added as well
     * @param target Optional root resource of the target
     */
    private void traverseTree(Resource resource, TraversingContext context, List<Resource> response, Resource source, Resource target) {
        if(resource != null && context.doAddChildren(resource)) {
            for(Resource child : resource.getChildren()) {
                parseProperties(child, context, response, source, target);
                traverseTree(child, context, response, source, target);
            }
        }
    }

    /**
     * References are in the properties of a resource and so here we look for the actual references
     * and add them if they are found. If transitive we check the reference resource as well
     * @param resource Resource which properties are checked here
     * @param context Traversing Context
     * @param response List containing the found references. It will contain any given resource only once
     * @param source Optional root resource of the source. If source and target is provided then missing parents are added as well
     * @param target Optional root resource of the target
     */
    private void parseProperties(Resource resource, TraversingContext context, List<Resource> response, Resource source, Resource target) {
        ValueMap properties = resource.getValueMap();
        for(Object item: properties.values()) {
            String value = item + "";
            for(String prefix: referencePrefixList) {
                if(value.startsWith(prefix)) {
                    log.trace("Found Reference Resource Path: '{}'", value);
                    final Resource child;
                    if(value.startsWith("/")) {
                        child = resource.getResourceResolver().getResource(value);
                    } else {
                        child = resource.getChild(value);
                    }
                    if(child != null && context.doAdd(child)) {
                        // Check if the resource is not already listed in there
                        if(containsResource(response, child)) {
                            log.trace("Resource is already in the list: '{}'", child);
                        } else {
                            if(source != null && target != null) {
                                listMissingParents(child, response, source, new MissingOrOutdatedResourceChecker(source, target));
                            }
                            log.trace("Found Reference Resource: '{}'", child);
                            response.add(child);
                            if(context.isTransitive() && context.doAddChildren(child)) {
                                checkResource(child, context, response, source, target);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Checks if the given list already contains the given resource by path **/
    private boolean containsResource(List<Resource> resources, Resource check) {
        boolean answer = false;
        String checkPath = check.getPath();
        for(Resource item: resources) {
            if(item.getPath().equals(checkPath)) {
                answer = true;
                break;
            }
        }
        return answer;
    }

    /**
     * This traverses the resource's children to look for referenced by resources. Call is ignored
     * if resource or reference path is not defined
     * @param resource Parent resource
     * @param referencePath Reference Path to look for
     * @param response List of resources found
     */
    private void traverseTreeReverse(Resource resource, String referencePath, List<Reference> response) {
        if(resource != null && isNotEmpty(referencePath)) {
            for(Resource child : resource.getChildren()) {
                parsePropertiesReverse(child, referencePath, response);
                traverseTreeReverse(child, referencePath, response);
            }
        }
    }

    /**
     * Check the given resource's properties to look for a property value that contains the given reference path.
     * Call is ignored if resource or reference path is not defined
     * @param resource Resource to be checked
     * @param referencePath Reference Path to look for
     * @param response List of resources found
     */
    private void parsePropertiesReverse(Resource resource, String referencePath, List<Reference> response) {
        if(resource != null && isNotEmpty(referencePath)) {
            ValueMap properties = resource.getValueMap();
            for(Map.Entry<String, Object> entry : properties.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue() + "";
                if(referencePath.equals(value)) {
                    // Find the node
                    boolean found = false;
                    Resource temp = resource;
                    while(true) {
                        if(temp.getName().equals(JCR_CONTENT)) {
                            Resource parent = temp.getParent();
                            if(parent != null) {
                                if(!response.contains(parent)) {
                                    response.add(new Reference(parent, name, resource));
                                }
                                found = true;
                            } else {
                                log.warn("JCR Content Node: '{}' found but no parent", temp.getPath());
                            }
                            break;
                        } else {
                            temp = temp.getParent();
                            if(temp == null) {
                                break;
                            }
                        }
                    }
                    if(!found) {
                        // No JCR Content node found so just use this one
                        if(!response.contains(resource)) {
                            response.add(new Reference(resource, name, resource));
                        }
                    }
                }
            }
        }
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }

    private void setup(Configuration configuration) {
        String[] prefixes = configuration.referencePrefix();
        referencePrefixList = new ArrayList<>();
        for(String prefix: prefixes) {
            if(prefix != null && !prefix.isEmpty()) {
                log.debug("Add Reference Prefix: '{}'", prefix);
                referencePrefixList.add(prefix);
            }
        }
        String[] roots = configuration.referencedByRoot();
        referencedByRootList = new ArrayList<>();
        for(String root: roots) {
            if(root != null && !root.isEmpty()) {
                log.debug("Add Referenced By Root: '{}'", root);
                referencedByRootList.add(root);
            }
        }
    }

}
