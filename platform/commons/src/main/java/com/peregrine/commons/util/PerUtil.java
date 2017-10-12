package com.peregrine.commons.util;

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

import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.DASH;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;

/**
 * Created by Andreas Schaefer on 5/26/17.
 */
public class PerUtil {

    public static final String RENDITIONS = "renditions";
    public static final String METADATA = "metadata";
    public static final String TEMPLATE = "template";

    public static final String PER_VENDOR = "headwire.com, Inc";
    public static final String PER_PREFIX = "Peregrine: ";
    public static final String EQUALS = "=";
    public static final String GET = "GET";
    public static final String POST = "POST";

    public static final String ENTRY_NOT_KEY_VALUE_PAIR = "Entry: '%s' could not be split into a key value pair, entries: '%s'";

    private static final Logger LOG = LoggerFactory.getLogger(PerUtil.class);
    public static final String RESOURCE_RESOLVER_FACTORY_CANNOT_BE_NULL = "Resource Resolver Factory cannot be null";
    public static final String SERVICE_NAME_CANNOT_BE_EMPTY = "Service Name cannot be empty";

    /** @return True if the given text is either null or empty **/
    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    /** @return True if the given text is both not null and not empty **/
    public static boolean isNotEmpty(String text) {
        return text != null && !text.isEmpty();
    }

    /**
     * Taks an array of strings and puts any non-empty string into the returned list
     * @param entries Array to be placed. If can be a null or empty array
     * @return List of strings that contains each entry of the given array that was not empty
     */
    public static List<String> intoList(String[] entries) {
        List<String> answer = new ArrayList<>();
        if(entries != null) {
            for(String entry: entries) {
                if(isNotEmpty(entry)) {
                    answer.add(entry);
                }
            }
        }
        return answer;
    }

    /**
     * Splits the given text into parts and add them to a list
     * @param text Text to be split. If null there is no splitting
     * @param separator Separator to be used to split the text. If null then text is just added if not null.
     * @return List of the split values which is never null but maybe empty
     */
    public static List<String> split(String text, String separator) {
        List<String> answer = new ArrayList<>();
        if(isNotEmpty(text)) {
            if(isNotEmpty(separator)) {
                String[] tokens = text.split(separator);
                answer.addAll(Arrays.asList(tokens));
            } else {
                answer.add(text);
            }
        }
        return answer;
    }

    /**
     * Splits the given List of strings into properties
     * @param entries List of string to be split. If null or empty the returned map is empty.
     *                All values that does not contain one and only one separator string are ignored
     * @param separator The string that separates the property name from its value
     * @return Properties Map which can be empty but never null
     */
    public static Map<String, Object> splitIntoProperties(List<String> entries, String separator) {
        Map<String, Object> answer = new HashMap<>();
        if(entries != null) {
            for(String entry: entries) {
                List<String> props = split(entry, separator);
                if(props.size() == 2) {
                    answer.put(props.get(0), props.get(1));
                } else {
                    LOG.warn("Property Entry: '{}' does not contain two entry separated: '{}'", entry, separator);
                }
            }
        }
        return answer;
    }

    /**
     * Splits the given array of texts into a map
     * @param entries Array of entries to be split. If the entries is null then there is no splitting, any null or empty item is ignored
     * @param keySeparator The separator between the key and value. If null then there is splitting
     * @param valueSeparator The separator between the parts of the values. If null then the value is added as single value
     * @return Map of the split entries
     */
    public static Map<String, List<String>> splitIntoMap(String[] entries, String keySeparator, String valueSeparator) {
        Map<String, List<String>> answer = new LinkedHashMap<>();
        if(entries != null && isNotEmpty(keySeparator)) {
            for(String entry: entries) {
                if(isNotEmpty(entry)) {
                    List<String> keyValue = split(entry, keySeparator);
                    if(keyValue.size() != 2) {
                        throw new IllegalArgumentException(String.format(ENTRY_NOT_KEY_VALUE_PAIR, entry, Arrays.asList(entries)));
                    }
                    String key = keyValue.get(0);
                    String value = keyValue.get(1);
                    List<String> values = split(value, valueSeparator);
                    answer.put(key, values);
                }
            }
        }
        return answer;
    }

    /**
     * Splits the given array of texts into a parameter map
     * @param entries Array of entries to be split. If the entries is null then there is no splitting, any null or empty item is ignored
     * @param keySeparator The separator between the key and value. If null then there is no splitting
     * @param valueSeparator The separator between the parts of the values. If null then the value is added as single value
     * @param parameterSeparator The separator between the parts of the parameter value. If null then there is no splitting
     * @return Map of the split entries
     */
    public static Map<String, Map<String, String>> splitIntoParameterMap(String[] entries, String keySeparator, String valueSeparator, String parameterSeparator) {
        Map<String, Map<String, String>> answer = new LinkedHashMap<>();
        if(entries != null && isNotEmpty(keySeparator)) {
            for(String entry: entries) {
                if(isNotEmpty(entry)) {
                    List<String> keyValue = split(entry, keySeparator);
                    switch(keyValue.size()) {
                        case 0:
                            continue;
                        case 1:
                            String key = keyValue.get(0);
                            Map<String, String> parameters = new LinkedHashMap<>();
                            answer.put(key, parameters);
                            break;
                        case 2:
                            key = keyValue.get(0);
                            String value = keyValue.get(1);
                            List<String> values = split(value, valueSeparator);
                            parameters = new LinkedHashMap<>();
                            answer.put(key, parameters);
                            for(String aValue: values) {
                                List<String> parameterList = split(aValue, parameterSeparator);
                                if(parameterList.size() != 2) {
                                    throw new IllegalArgumentException(String.format(ENTRY_NOT_KEY_VALUE_PAIR, aValue, entries));
                                }
                                parameters.put(parameterList.get(0), parameterList.get(1));
                            }
                            break;
                        default:
                            throw new IllegalArgumentException(String.format(ENTRY_NOT_KEY_VALUE_PAIR, entry, entries));
                    }
                }
            }
        }
        return answer;
    }

    /**
     * Provides the relative path of a resource to a given root
     * @param root Root Resource
     * @param child Child Resource
     * @return Path from the Root to the Child if Child is a sub node of the
     *         root otherwise null
     */
    public static String relativePath(Resource root, Resource child) {
        String answer = null;
        String rootPath = root.getPath();
        String childPath = child.getPath();
        if(childPath.startsWith(rootPath) && childPath.length() > rootPath.length() + 1) {
            answer = childPath.substring(rootPath.length() + 1);
        }
        return answer;
    }

    /**
     * Get the path of the parent of the given child path
     * @param childPath JCR Path of the child node we want to find the parent
     * @return JCR Path of the parent if found otherwise null
     */
    public static String getParent(String childPath) {
        String answer = null;
        if(childPath != null && !childPath.isEmpty()) {
            int index = childPath.lastIndexOf('/');
            if(index > 1 && index < childPath.length() - 1) {
                answer = childPath.substring(0, index);
            }
        }
        return answer;
    }

    /**
     * Obtains the resource with a given path
     * @param source Starting resource if the path is relative
     * @param path Path of the resource to be found. If relative it is assumed to be a child of the source
     * @return Resource if found otherwise null. If the source or path is null then this is null as well
     */
    public static Resource getResource(Resource source, String path) {
        Resource answer = null;
        if(source != null && path != null) {
            if(path.startsWith("/")) {
                answer = source.getResourceResolver().getResource(path);
            } else {
                answer = source.getChild(path);
            }
        } else {
            if(source == null) {
                LOG.warn("Source is null so path: '{}' cannot be resolved", path);
            } else {
                LOG.warn("Path is null so call is ignored");
            }
        }
        return checkResource(answer);
    }

    /**
     * Tries to find the resource of the given path with the resource resolver
     * @param resourceResolver Used to obtain the resource
     * @param path Path of the resource to be found
     * @return A resource if found and if it exists otherwise null
     */
    public static Resource getResource(ResourceResolver resourceResolver, String path) {
        Resource answer = null;
        if(resourceResolver != null && path != null) {
            answer = resourceResolver.getResource(path);
        } else {
            if(resourceResolver == null) {
                LOG.warn("Resource Resolver is null so path: '{}' cannot be resolved", path);
            } else {
                LOG.warn("Path is null so call with RR is ignored");
            }
        }
        return checkResource(answer);
    }

    /**
     * Checks if the given resource exists
     * @param resource Resource to be checked
     * @return The given resource if it is not null and does exist (is not a Non Existing Resource ref) otherwise null
     */
    public static Resource checkResource(Resource resource) {
        if(resource != null) {
            return ResourceUtil.isNonExistingResource(resource) ? null : resource;
        } else {
            return null;
        }
    }

    /**
     * Gets the Value Map of the Resource JCR Content
     *
     * @param resource Resource to look for the properties. If this is not the jcr:content node
     *                 then it will try to obtain the jcr:content node automatically
     * @return The Value Map of the JCR Content node if found otherwise the resource's value map. If
     *         resource is null then it will return null
     */
    public static ValueMap getProperties(Resource resource) {
        return getProperties(resource, true);
    }

    /**
     * Gets the Value Map of the Resource or its JCR Content
     *
     * @param resource Resource to look for the properties
     * @param goToJcrContent If true then if the given resource is not the JCR Content it will look that one up
     * @return The Value Map of the Resource or JCR Content node
     */
    public static ValueMap getProperties(Resource resource, boolean goToJcrContent) {
        ValueMap answer = null;
        Resource jcrContent = resource;
        if(goToJcrContent && !jcrContent.getName().equals(PerConstants.JCR_CONTENT)) {
            jcrContent = jcrContent.getChild(PerConstants.JCR_CONTENT);
        }
        if(jcrContent != null) {
            answer = jcrContent.getValueMap();
        }
        return answer;
    }

    /**
     * Gets the Modifiable Value Map of the Resource JCR Content
     *
     * @param resource Resource to look for the properties. If this is not the jcr:content node
     *                 then it will try to obtain the jcr:content node automatically
     * @return The Modifiable Value Map of the JCR Content node if found otherwise the resource's value map. If
     *         resource is null then it will return null
     */
    public static ModifiableValueMap getModifiableProperties(Resource resource) {
        return getModifiableProperties(resource, true);
    }

    /**
     * Gets the Modifiable Value Map of the Resource or its JCR Content
     *
     * @param resource Resource to look for the properties
     * @param goToJcrContent If true then if the given resource is not the JCR Content it will look that one up
     * @return The Modifiable Value Map of the Resource or JCR Content node
     */
    public static ModifiableValueMap getModifiableProperties(Resource resource, boolean goToJcrContent) {
        ModifiableValueMap answer = null;
        Resource jcrContent = resource;
        if(goToJcrContent && !jcrContent.getName().equals(PerConstants.JCR_CONTENT)) {
            jcrContent = jcrContent.getChild(PerConstants.JCR_CONTENT);
        }
        if(jcrContent != null) {
            answer = jcrContent.adaptTo(ModifiableValueMap.class);
        }
        return answer;
    }

    /**
     * Lists all the parent nodes between the child and the root if the root is one
     * of the child's parents. Both child and root and not included in the returned list
     * @param root
     * @param child
     * @return
     */
    public static List<Resource> listParents(Resource root, Resource child) {
        List<Resource> answer = new ArrayList<>();
        Resource parent = child.getParent();
        while(true) {
            if(parent == null) {
                // No parent matches 'source' so we ignore it
                answer.clear();
                break;
            }
            if(parent.getPath().equals(root.getPath())) {
                // Hit the source -> done with loop
                break;
            }
            answer.add(parent);
            parent = parent.getParent();
        }
        return answer;
    }

    /**
     * Goes recursively through the resource tree and adds all resources that are selected by the resource checker.
     * The JCR Content is traversed by default (except when the Resource Checker prevents it) but the other children
     * only when the deep flag is set true
     *
     * @param startingResource Root resource of the search
     * @param response List of resources where the missing resources are added to
     * @param resourceChecker Resource Checker instance that decides which resource is deemed missing and defines
     *                         if children resources are traversed
     * @param deep If true this goes down recursively any children
     */
    public static void listMissingResources(Resource startingResource, List<Resource> response, ResourceChecker resourceChecker, boolean deep) {
        ResourceChecker childResourceChecker = resourceChecker;
        if(startingResource != null && resourceChecker != null && response != null) {
            if(resourceChecker.doAdd(startingResource)) {
                response.add(startingResource);
                // If this is JCR Content we need to add all children
                if(startingResource.getName().equals(PerConstants.JCR_CONTENT)) {
                    childResourceChecker = new AddAllResourceChecker();
                }
            }
            if(resourceChecker.doAddChildren(startingResource)) {
                for(Resource child : startingResource.getChildren()) {
                    if(child.getName().equals(PerConstants.JCR_CONTENT)) {
                        listMissingResources(child, response, childResourceChecker, true);
                    } else if(deep) {
                        listMissingResources(child, response, childResourceChecker, true);
                    }
                }
            }
        }
    }

//AS TODO: This seems to be a duplicate of the method above?
//    public static void listMatchingResources(Resource startingResource, List<Resource> response, ResourceChecker resourceChecker, boolean deep) {
//        ResourceChecker childResourceChecker = resourceChecker;
//        if(startingResource != null && resourceChecker != null && response != null) {
//            if(resourceChecker.doAdd(startingResource)) {
//                response.add(startingResource);
//                // If this is JCR Content we need to add all children
//                if(startingResource.getName().equals(PerConstants.JCR_CONTENT)) {
//                    childResourceChecker = new AddAllResourceChecker();
//                }
//            }
//            if(resourceChecker.doAddChildren(startingResource)) {
//                for(Resource child : startingResource.getChildren()) {
//                    if(child.getName().equals(PerConstants.JCR_CONTENT)) {
//                        listMatchingResources(child, response, childResourceChecker, true);
//                    } else if(deep) {
//                        listMatchingResources(child, response, childResourceChecker, true);
//                    }
//                }
//            }
//        }
//    }
//
//    public static boolean containsResource(List<Resource> resourceList, Resource resource) {
//        for(Resource item: resourceList) {
//            if(item.getPath().equals(resource.getPath())) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Lists all the missing parents compared to the parents on the source
     *
     * @param startingResource Child Resource
     * @param response List of resources to which the missing parents are added to. Cannot be null
     * @param source Root of the Child
     * @param resourceChecker Resource Check instance that defined when a parent is added to the missing list
     */
    public static void listMissingParents(Resource startingResource, List<Resource> response, Resource source, ResourceChecker resourceChecker) {
        if(startingResource != null && source != null && resourceChecker != null && response != null) {
            List<Resource> parents = listParents(source, startingResource);
            // Now we go through all parents, check if the matching parent exists on the target
            // side and if not there add it to the list
            for(Resource sourceParent : parents) {
                if(resourceChecker.doAdd(sourceParent)) {
                    response.add(sourceParent);
                }
            }
        }
    }

    /**
     * Tries to obtain the Service Resource Resolver
     * @param resolverFactory Resource Resolver Factory which cannot be null
     * @param serviceName Name of the service to find its resource resolver
     * @return Resource Resolver for that Service
     * @throws LoginException If the resolver factory could not obtain the Service Resource Resolver
     * @throws IllegalArgumentException If the resource resolver is null or the service name is empty
     */
    public static ResourceResolver loginService(ResourceResolverFactory resolverFactory, String serviceName) throws LoginException {
        if(resolverFactory == null) { throw new IllegalArgumentException(RESOURCE_RESOLVER_FACTORY_CANNOT_BE_NULL); }
        if(isEmpty(serviceName)) { throw new IllegalArgumentException(SERVICE_NAME_CANNOT_BE_EMPTY); }
        Map<String, Object> authInfo = new HashMap<String, Object>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, serviceName);
        return resolverFactory.getServiceResourceResolver(authInfo);
    }

    /**
     * Adjust an Image Category / Tag Name to be suitable for Peregrine
     * @param name Name to be adjust
     * @return The given name lowercase and spaces and slashes to underscore or if name is null then null
     */
    public static String adjustMetadataName(String name) {
        return name == null ?
            null :
            name.toLowerCase().replaceAll(" ", "_").replaceAll("/", "_");
    }

    /**
     * Check if the given resource has that Sling Resource Type
     * @param resource Resource to be checked. It will test this resource and not go down to JCR Content
     * @param resourceType Sling Resource Type to test. If null or empty this method returns false
     * @return true if the resource contains a Sling Resource Type that matches the given value
     */
    public static boolean isResourceType(Resource resource, String resourceType) {
        String answer = null;
        if(resource != null) {
            ValueMap properties = getProperties(resource, false);
            answer = properties.get(SLING_RESOURCE_TYPE, String.class);
        }
        return answer != null && answer.equals(resourceType);
    }

    /**
     * Check if the given resource has that Primary Type
     * @param resource Resource to be checked. It will test this resource and not go down to JCR Content
     * @param primaryType Primary Type to test. If null or empty this method returns false
     * @return true if the resource contains a Primary Type that matches the given value
     */
    public static boolean isPrimaryType(Resource resource, String primaryType) {
        String answer = null;
        if(resource != null) {
            ValueMap properties = getProperties(resource, false);
            answer = properties.get(JCR_PRIMARY_TYPE, String.class);
        }
        return answer != null && answer.equals(primaryType);
    }

    /**
     * @param resource Given resource
     * @return Returns the JCR Primary Type property from the resource if that one is not null and if it is found
     */
    public static String getPrimaryType(Resource resource) {
        String answer = null;
        if(resource != null) {
            ValueMap properties = getProperties(resource, false);
            answer = properties.get(JCR_PRIMARY_TYPE, String.class);
        }
        return answer;
    }

    /** @return Returns the Sling Resource Type of the resource or resource's jcr:content node. Returns null if resource is null or not found **/
    public static String getResourceType(Resource resource) {
        String answer = null;
        if(resource != null) {
            ValueMap properties = getProperties(resource, true);
            if(properties == null) {
                properties = getProperties(resource, false);
            }
            if(properties != null) {
                answer = properties.get(SLING_RESOURCE_TYPE, String.class);
            }
        }
        return answer;
    }

    /** @return The Mime Type of this resource (in the JCR Content resource) **/
    public static String getMimeType(Resource resource) {
        String answer = null;
        if(resource != null) {
            ValueMap properties = getProperties(resource, true);
            if(properties != null) {
                answer = properties.get(JCR_MIME_TYPE, String.class);
            }
        }
        return answer;
    }

    /** Resource Check interface **/
    public static interface ResourceChecker {
        /** @return True if the resource checks out **/
        public boolean doAdd(Resource resource);
        /** @return False if the resource's children should not be considered **/
        public boolean doAddChildren(Resource resource);
    }

    /** Checks all resources that are either missing or are outdated on the target **/
    public static class MissingOrOutdatedResourceChecker
        implements ResourceChecker
    {
        private Resource source;
        private Resource target;

        /**
         * This class will map any children of the source resource to a
         * child on the target (same relative child path). If missing or
         * outdated then it will be checked
         *
         * @param source Source Root Resource
         * @param target Target Root Resource
         */
        public MissingOrOutdatedResourceChecker(Resource source, Resource target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean doAdd(Resource resource) {
            boolean answer = false;
            String relativePath = relativePath(source, resource);
            Resource targetResource = target.getChild(relativePath);
            LOG.trace("Do Add. Resource: '{}', relative path: '{}', target resource: '{}'", resource.getPath(), relativePath, targetResource);
            if(targetResource == null) {
                answer = true;
            } else {
                //AS TODO This does not work as is. We need to compare the source's last modified timestamp against the target's
                //AS TODO replicated timestamp
                Calendar sourceLastModified = resource.getValueMap().get(PER_REPLICATED, Calendar.class);
                Calendar targetLastModified = targetResource.getValueMap().get(PER_REPLICATED, Calendar.class);
                if(sourceLastModified != null && targetLastModified != null) {
                    answer = sourceLastModified.after(targetLastModified);
                }
            }
            return answer;
        }

        @Override
        public boolean doAddChildren(Resource resource) { return true; }
    }

    /**
     * Checks all resources that exist on the target (same relative path
     * as on the source)
     */
    public static class MatchingResourceChecker
        implements ResourceChecker
    {
        private Resource source;
        private Resource target;

        public MatchingResourceChecker(Resource source, Resource target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean doAdd(Resource resource) {
            String relativePath = relativePath(source, resource);
            Resource targetResource = target.getChild(relativePath);
            return targetResource != null;
        }

        @Override
        public boolean doAddChildren(Resource resource) { return true; }
    }

    /** Checks all resources **/
    public static class AddAllResourceChecker
        implements ResourceChecker
    {
        @Override
        public boolean doAdd(Resource resource) {
            return true;
        }
        @Override
        public boolean doAddChildren(Resource resource) { return true; }
    }

    /**
     * Obtains the Component Name form the Resource
     * @param resource Given Resource
     * @return Takes the resource type, takes away leading slash and then makes it lowercase and replaces / with -.
     *         It also splits words by camel case with a -.
     *         For example: '/one/twoThree/FourFive'
     *         Will Yield: 'one-two-three--four-five'
     *         The double hyphen is due to the / and uppercase F in Four
     */
    public static String getComponentNameFromResource(Resource resource) {
        String resourceType = resource.getResourceType();
        if (resourceType != null) {
            if(resourceType.startsWith("/")) {
                resourceType = StringUtils.substringAfter(resourceType, SLASH);
            }
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, resourceType.replaceAll(SLASH, DASH));
        } else {
            return "";
        }
    }
}
