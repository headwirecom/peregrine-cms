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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.*;

import static com.peregrine.commons.util.PerConstants.*;
import static org.apache.commons.lang3.StringUtils.*;

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

    public static final String RESOURCE_RESOLVER_FACTORY_CANNOT_BE_NULL = "Resource Resolver Factory cannot be null";
    public static final String SERVICE_NAME_CANNOT_BE_EMPTY = "Service Name cannot be empty";

    private static final Logger LOG = LoggerFactory.getLogger(PerUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public static String extractName(final String path) {
        if (isNotBlank(path)) {
            final int index = path.lastIndexOf('/');
            if (index < path.length() - 1) {
                return path.substring(index + 1);
            }
        }

        return null;
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
    public static ValueMap getProperties(final Resource resource, final boolean goToJcrContent) {
        return Optional.ofNullable(goToJcrContent ? getJcrContent(resource) : resource)
                .map(Resource::getValueMap)
                .orElse(null);
    }

    public static Resource getJcrContent(final Resource resource) {
        if (PerConstants.JCR_CONTENT.equals(resource.getName())) {
            return resource;
        }

        return resource.getChild(PerConstants.JCR_CONTENT);
        }

    public static Resource getJcrContentOrSelf(final Resource resource) {
        return Optional.ofNullable(getJcrContent(resource))
                .orElse(resource);
    }

    public static ValueMap getJcrContentOrSelfProperties(final Resource resource) {
        return Optional.ofNullable(getJcrContentOrSelf(resource))
                .map(Resource::getValueMap)
                .orElse(null);
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
        return Optional.ofNullable(goToJcrContent ? getJcrContent(resource) : resource)
                .map(r -> r.adaptTo(ModifiableValueMap.class))
                .orElse(null);
    }

    /**
     * Lists all the parent nodes between the child and the root if the root is one
     * of the child's parents. Both child and root and not included in the returned list
     * @param root
     * @param child
     * @return
     */
    public static List<Resource> listParents(final Resource root, final Resource child) {
        final List<Resource> answer = new ArrayList<>();
        Resource parent = child.getParent();
        while(true) {
            if(parent == null) {
                // No parent matches 'source' so we ignore it
                answer.clear();
                return answer;
            }
            if(parent.getPath().equals(root.getPath())) {
                // Hit the source -> done with loop
                return answer;
            }
            answer.add(parent);
            parent = parent.getParent();
        }
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
    public static void listMissingResources(
            final Resource startingResource,
            final List<Resource> response,
            final ResourceChecker resourceChecker,
            final boolean deep) {
        ResourceChecker childResourceChecker = resourceChecker;
        if (startingResource == null || resourceChecker == null || response == null) {
            return;
        }

            if(resourceChecker.doAdd(startingResource)) {
                if(!containsResource(response, startingResource)) {
                    response.add(startingResource);
                }
                // If this is JCR Content we need to add all children
            if (PerConstants.JCR_CONTENT.equals(startingResource.getName())) {
                    childResourceChecker = new AddAllResourceChecker();
                }
            }

        if (!resourceChecker.doAddChildren(startingResource)) {
            return;
        }

        for (final Resource child : startingResource.getChildren()) {
            if (deep || PerConstants.JCR_CONTENT.equals(child.getName())) {
                        listMissingResources(child, response, childResourceChecker, true);
                    }
                }
            }

    public static boolean containsResource(final List<Resource> resources, final Resource check) {
        if (check == null) {
            return true;
    }

        final String path = check.getPath();
        for (final Resource item : resources) {
                if(path.equals(item.getPath())) {
                return true;
            }
        }

        return false;
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
    public static void listMissingParents(
            final Resource startingResource,
            final List<Resource> response,
            final Resource source,
            final ResourceChecker resourceChecker) {
        if (startingResource == null || source == null || resourceChecker == null || response == null) {
            return;
        }

            // Now we go through all parents, check if the matching parent exists on the target
            // side and if not there add it to the list
        for (final Resource sourceParent : listParents(source, startingResource)) {
            if (resourceChecker.doAdd(sourceParent) && !containsResource(response, sourceParent)) {
                        response.add(sourceParent);
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
    public static ResourceResolver loginService(final ResourceResolverFactory resolverFactory, final String serviceName) throws LoginException {
        if (resolverFactory == null) {
            throw new IllegalArgumentException(RESOURCE_RESOLVER_FACTORY_CANNOT_BE_NULL);
        }

        if (isEmpty(serviceName)) {
            throw new IllegalArgumentException(SERVICE_NAME_CANNOT_BE_EMPTY);
        }

        final Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, serviceName);
        return resolverFactory.getServiceResourceResolver(authInfo);
    }

    /**
     * Adjust an Image Category / Tag Name to be suitable for Peregrine
     * @param name Name to be adjust
     * @return The given name lowercase and spaces and slashes to underscore or if name is null then null
     */
    public static String adjustMetadataName(final String name) {
        return Optional.ofNullable(name)
                .map(String::toLowerCase)
                .map(s -> s.replace(" ", "_"))
                .map(s -> s.replace("/", "_"))
                .orElse(null);
    }

    public static boolean isPropertyEqual(final Resource resource, final String propertyName, final String value) {
        return Optional.ofNullable(resource)
                .map(r -> getProperties(r, false))
                .map(p -> p.get(propertyName, String.class))
                .map(v -> v.equals(value))
                .orElse(false);
    }

    /**
     * Check if the given resource has that Sling Resource Type
     * @param resource Resource to be checked. It will test this resource and not go down to JCR Content
     * @param resourceType Sling Resource Type to test. If null or empty this method returns false
     * @return true if the resource contains a Sling Resource Type that matches the given value
     */
    public static boolean isResourceType(final Resource resource, final String resourceType) {
        return isPropertyEqual(resource, SLING_RESOURCE_TYPE, resourceType);
    }

    /**
     * Check if the given resource has that Primary Type
     * @param resource Resource to be checked. It will test this resource and not go down to JCR Content
     * @param primaryType Primary Type to test. If null or empty this method returns false
     * @return true if the resource contains a Primary Type that matches the given value
     */
    public static boolean isPrimaryType(final Resource resource, final String primaryType) {
        return isPropertyEqual(resource, JCR_PRIMARY_TYPE, primaryType);
    }

    /**
     * @param resource Given resource
     * @return Returns the JCR Primary Type property from the resource if that one is not null and if it is found
     */
    public static String getPrimaryType(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(r -> getProperties(r, false))
                .map(props -> props.get(JCR_PRIMARY_TYPE, String.class))
                .orElse(null);
    }

    /** @return Returns the Sling Resource Type of the resource or resource's jcr:content node. Returns null if resource is null or not found **/
    public static String getResourceType(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(PerUtil::getJcrContentOrSelfProperties)
                .map(props -> props.get(SLING_RESOURCE_TYPE, String.class))
                .orElse(null);
    }

    /** @return The Mime Type of this resource (in the JCR Content resource) **/
    public static String getMimeType(final Resource resource) {
        String answer = null;
        if(resource != null) {
            ValueMap properties = getProperties(resource);
            if(properties != null) {
                answer = properties.get(JCR_MIME_TYPE, String.class);
            }
        }
        return answer;
    }

    /**
     * Extracts a property from the given map and if found returns the
     * string representation otherwise null
     * @param source Source Map
     * @param key Property Name
     * @return Property Value as string otherwise null
     */
    public static String getStringOrNull(Map<?, ?> source, String key) {
        String answer = null;
        if(source != null && source.containsKey(key)) {
            Object temp = source.get(key);
            if(temp != null) {
                answer = temp.toString();
            }
        }
        return answer;
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
    public static String getComponentNameFromResource(final Resource resource) {
        final String normalized = normalizeResourceTypeName(resource.getResourceType());
        if (isBlank(normalized)) {
            return EMPTY;
        }

        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, normalized);
    }

    public static String getComponentVariableNameFromString(final String resourceType) {
        final String normalized = normalizeResourceTypeName(resourceType);
        if (isBlank(normalized)) {
            return EMPTY;
        }

        return "cmp" + CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, normalized);
    }

    private static String normalizeResourceTypeName(final String resourceType) {
        if (isBlank(resourceType)) {
            return resourceType;
        }

        String result = resourceType;
        if (result.startsWith(SLASH)) {
            result = substringAfter(result, SLASH);
        }

        return result.replace(SLASH, DASH);
    }

    /**
     * Tries to convert the given String into a Map if this is an JSon Object
     * @param json String that must represent a JSon Object
     * @return Map representing the JSon Object
     * @throws IOException If it could not been converted
     */
    public static Map convertToMap(final String json) throws IOException {
        if (json != null) {
            return OBJECT_MAPPER.readValue(json, LinkedHashMap.class);
        }

        return new LinkedHashMap<>();
    }

    public static boolean doSave(ResourceResolver resourceResolver, String action) {
        boolean answer = false;
        Session session = resourceResolver.adaptTo(Session.class);
        if(session == null) {
            LOG.warn("Could not obtain Session to save changes for: '{}'", action);
        } else {
            try {
                session.save();
                answer = true;
            } catch (RepositoryException e) {
                LOG.warn("Failed to save changes for: '{}'", action, e);
            }
        }
        return answer;
    }

    public static boolean isNullOrTrue(final Object value) {
        return value == null || Boolean.TRUE.toString().equalsIgnoreCase(String.valueOf(value));
    }

    public static String getString(final Map map, final Object key) {
        return getString(map, key, null);
    }

    public static String getString(final Map map, final Object key, final String defaultValue) {
        String answer = defaultValue;
        final Object value = map.get(key);
        if (value != null && !value.toString().isEmpty()) {
            answer = value.toString();
        }
        return answer;
    }

    public static boolean getBoolean(final Map map, final Object key, boolean defaultValue) {
        boolean answer = defaultValue;
        final Object value = map.get(key);
        if (value != null && !value.toString().isEmpty()) {
            answer = "true".equalsIgnoreCase(value.toString());
        }
        return answer;
    }

    public static int getChildIndex(final Resource parent, final Resource child) {
        if (parent == null || child == null) {
            return -1;
        }

        final String path = child.getPath();
        if (!StringUtils.equals(getParent(path), parent.getPath())) {
            return -1;
        }

        int index = 0;
        for (final Resource resource: parent.getChildren()) {
            if (path.equals(resource.getPath())) {
                return index;
            }

            index++;
        }

        return -1;
    }

    public static Resource getFirstChild(final Resource parent) {
        return Optional.ofNullable(parent)
                .map(Resource::getChildren)
                .map(Iterable::iterator)
                .filter(Iterator::hasNext)
                .map(Iterator::next)
                .orElse(null);
    }

    public static boolean isPropertyPresentAndEqualsTrue(final Node node, final String propertyName) {
        try {
            return node.hasProperty(propertyName)
                    && node.getProperty(propertyName).getBoolean();
        } catch (final RepositoryException e) {
            return false;
        }
    }

    public static Node getFirstChild(final Node parent) {
        try {
            final NodeIterator iterator = parent.getNodes();
            if (iterator.hasNext()) {
                return iterator.nextNode();
            }
        } catch (final RepositoryException e) {
            return null;
        }

        return null;
    }

    public static String toStringOrNull(final Object object) {
        return object == null ? null : object.toString();
    }

    public static Object getClassOrNull(final Object value) {
        return value == null ? null : value.getClass();
    }

    public static String getPropsFromMap(final Map source, final String key, final String defaultValue) {
        return defaultIfBlank(toStringOrNull(source.get(key)), defaultValue);
    }

    public static Node getNodeAtPosition(final Node parent, final int position) throws RepositoryException {
        final NodeIterator i = parent.getNodes();
        int counter = 0;
        while (i.hasNext()) {
            if (counter == position) {
                return i.nextNode();
            }

            i.nextNode();
            counter++;
        }

        return null;
    }

    public static Node getNode(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(r -> r.adaptTo(Node.class))
                .orElse(null);
    }

    public static Node getNode(final ResourceResolver resourceResolver, final String path) {
        return getNode(
                Optional.ofNullable(path)
                .map(p -> getResource(resourceResolver, p))
                .orElse(null)
        );
    }

    public static String getPath(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(Resource::getPath)
                .orElse(null);
    }

    /** Resource Check interface **/
    public interface ResourceChecker {
        /** @return True if the resource checks out **/
        boolean doAdd(Resource resource);
        /** @return False if the resource's children should not be considered **/
        boolean doAddChildren(Resource resource);
    }

    /** Checks all resources that are either missing or are outdated on the target **/
    public static class MissingOrOutdatedResourceChecker
        implements ResourceChecker
    {
        private final Resource source;
        private final Resource target;

        /**
         * This class will map any children of the source resource to a
         * child on the target (same relative child path). If missing or
         * outdated then it will be checked
         *
         * @param source Source Root Resource
         * @param target Target Root Resource
         */
        public MissingOrOutdatedResourceChecker(final Resource source, final Resource target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean doAdd(final Resource resource) {
            final String relativePath = relativePath(source, resource);
            final Resource targetResource = Optional.ofNullable(relativePath)
                    .map(target::getChild)
                    .orElse(null);
            LOG.trace("Do Add. Resource: '{}', relative path: '{}', target resource: '{}'", resource.getPath(), relativePath, targetResource);
            if(targetResource == null) {
                return true;
            }

                //AS TODO This does not work as is. We need to compare the source's last modified timestamp against the target's
                //AS TODO replicated timestamp
            final Calendar sourceLastModified = resource.getValueMap().get(PER_REPLICATED, Calendar.class);
            final Calendar targetLastModified = targetResource.getValueMap().get(PER_REPLICATED, Calendar.class);

            return sourceLastModified != null && targetLastModified != null
                    && sourceLastModified.after(targetLastModified);
        }

        @Override
        public boolean doAddChildren(final Resource resource) {
            return true;
        }
    }

    /**
     * Checks all resources that exist on the target (same relative path
     * as on the source)
     */
    public static class MatchingResourceChecker
        implements ResourceChecker
    {
        private final Resource source;
        private final Resource target;

        public MatchingResourceChecker(final Resource source, final Resource target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean doAdd(final Resource resource) {
            return Optional.ofNullable(relativePath(source, resource))
                    .map(target::getChild)
                    .isPresent();
        }

        @Override
        public boolean doAddChildren(final Resource resource) { return true; }
    }

    /** Checks all resources **/
    public static class AddAllResourceChecker
        implements ResourceChecker
    {
        @Override
        public boolean doAdd(final Resource resource) {
            return true;
        }
        @Override
        public boolean doAddChildren(Resource resource) { return true; }
    }
}
