package com.peregrine.admin.resource;

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
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.listMissingParents;

import com.peregrine.commons.util.PerUtil.MissingOrOutdatedResourceChecker;
import com.peregrine.replication.Reference;
import com.peregrine.replication.ReferenceLister;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.sling.api.resource.Resource;
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
public class ReferenceListerService
    implements ReferenceLister
{
    @ObjectClassDefinition(
        name = "Peregrine: Reference List Provider",
        description = "Provides a list of referenced resources for a given resource"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "ReferencePrefix",
            description = "List of Prefixes to find references in a resource",
            required = true
        )
        String[] referencePrefix() default "/content/";
        @AttributeDefinition(
            name = "ReferencedByRoot",
            description = "List of Roots to look for the referenced by resources",
            required = true
        )
        String[] referencedByRoot() default "/content";
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private List<String> referencePrefixList = new ArrayList<>();
    private List<String> referencedByRootList = new ArrayList<>();

    @Override
    public List<Resource> getReferenceList(boolean transitive, Resource resource, boolean deep) {
        return getReferenceList(transitive, resource, deep, null, null);
    }

    @Override
    public List<Resource> getReferenceList(boolean transitive, Resource resource, boolean deep, Resource source, Resource target) {
        List<Resource> answer = new ArrayList<>();
        TraversingContext context = new TraversingContext().setTransitive(transitive).setDeep(deep);
        checkResource(resource, context, answer, source, target);
        return answer;
    }

    @Override
    public List<Reference> getReferencedByList(Resource resource) {
        List<Reference> answer = new ArrayList<>();
        if(resource != null) {
            for(String root : referencedByRootList) {
                Resource rootResource = resource != null ? resource.getResourceResolver().getResource(root) : null;
                if(rootResource != null) {
                    traverseTreeReverse(rootResource, resource.getPath(), answer);
                }
            }
        }
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
        if(resource != null) {
            parseProperties(resource, context, response, source, target);
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
        if(resource != null) {
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
                    String resourcePath = value;
                    log.trace("Found Reference Resource Path: '{}'", resourcePath);
                    Resource child = null;
                    if(resourcePath.startsWith("/")) {
                        child = resource.getResourceResolver().getResource(value);
                    } else {
                        child = resource.getChild(resourcePath);
                    }
                    if(child != null) {
                        // Check if the resource is not already listed in there
                        if(containsResource(response, child)) {
                            log.trace("Resource is already in the list: '{}'", child);
                        } else {
                            if(source  != null && target != null) {
                                listMissingParents(child, response, source, new MissingOrOutdatedResourceChecker(source, target));
                            }
                            log.trace("Found Reference Resource: '{}'", child);
                            response.add(child);
                            if(context.isTransitive()) {
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

    /**
     * Traversing context providing necessary flags as well as defining which resource are processed
     * It also makes sure that we don't end up in an endless loop with cyclic references
     **/
    private static class TraversingContext {
        private boolean transitive = false;
        private boolean deep = false;
        private Set<String> deepLimits = new TreeSet<>();
        private Tree visited = new Tree();

        public TraversingContext() {};

        public TraversingContext setTransitive(boolean transitive) {
            this.transitive = transitive;
            return this;
        }

        public TraversingContext setDeep(boolean deep) {
            this.deep = deep;
            return this;
        }

        /** @return True if references in references should be listed as well **/
        public boolean isTransitive() {
            return transitive;
        }

        /** @return True if all children are traversed **/
        public boolean isDeep() {
            return deep;
        }

        /**
         * Checks the resource if it should be checked. If will not
         * be checked if not deep but outside of the marked deep paths
         * or if already visited. If not visited then this method will
         * add them to the visited list
         * @param resource Resource to be checked
         * @return TRUE if we are going deep and are not visited yet or
         *         are in the deep limited paths and not visited yet
         */
        public boolean proceed(Resource resource) {
            boolean answer = false;
            if(resource != null) {
                String path = resource.getPath();
                if(!visited.contains(path)) {
                    visited.addChildByPath(path);
                    if(!deep) {
                        for(String limit : deepLimits) {
                            if(path.startsWith(limit)) {
                                answer = true;
                                break;
                            }
                        }
                    } else {
                        answer = true;
                    }
                }
            }
            return answer;
        }

        /**
         * Adds an exempt starting path if traversing is not deep
         * @param path Exempt path to be added. In order to make this work the value
         *             must start with a slash
         */
        public TraversingContext addDeepLimit(String path) {
            if(isNotEmpty(path) && !deepLimits.contains(path)) {
                deepLimits.add(path);
            }
            return this;
        }
    }

    /**
     *  Root Object of a Folder Name Base Tree. It allows to
     *  add children by a JCR Node Path
     **/
    public static class Tree
        extends Node
    {
        public Tree() {
            super(null, SLASH);
        }

        void setParent(Node parent) {}

        /**
         * Checks if the given path exists in this tree
         * @param path JCR Resource Path to be checked
         * @return True if for all JCR resources of the path there is a corresponding node
         */
        public boolean contains(String path) {
            boolean answer = false;
            if(isNotEmpty(path)) {
                answer = true;
                String[] tokens = path.split(SLASH);
                Node node = this;
                for(String token: tokens) {
                    if(isNotEmpty(token)) {
                        Node child = node.getChild(token);
                        if(child != null) {
                            node = child;
                        } else {
                            answer = false;
                            break;
                        }
                    }
                }
            }
            return answer;
        }

        /**
         * Creates a node for a resources in the given path if they don't already exist
         * @param path JCR Resource Path separated by a slash
         * @return This tree instance
         */
        public Tree addChildByPath(String path) {
            if(isEmpty(path)) { throw new IllegalArgumentException("Child Path must be provided"); }
            String[] tokens = path.split(SLASH);
            Node node = this;
            for(String token: tokens) {
                if(isNotEmpty(token)) {
                    Node child = node.getChild(token);
                    if(child != null) {
                        node = child;
                    } else {
                        node = node.addChild(token);
                    }
                }
            }
            return this;
        }

        @Override
        public String toString() {
            return "Tree(" + super.toString() + ")";
        }
    }

    /**
     * Node Entry of the Folder Tree
     * This represents a JCR Resource in a path
     */
    public static class Node {
        private String segment;
        private Node parent;
        private List<Node> children;

        /**
         * Creates a node with a given parent and name
         * @param parent Parent Node of this node. This node will be added as child to this parent here
         * @param segment Resource Name
         */
        public Node(Node parent, String segment) {
            setParent(parent);
            if(isEmpty(segment)) {
                throw new IllegalArgumentException("Node Segment must be defined");
            }
            this.segment = segment;
        }

        /** Resource Name **/
        public String getSegment() {
            return segment;
        }

        /**
         * Sets the given Node as parent and adds itself as child to that parent **
         * @parent Parent Node which cannot be null
         */
        void setParent(Node parent) {
            if(parent == null) { throw new IllegalArgumentException("Parent Node must be defined"); }
            this.parent = parent;
            parent.addChild(this);
        }

        /** @return A node with the given resource name if found otherwise null **/
        public Node getChild(String segment) {
            Node answer = null;
            if(children != null && isNotEmpty(segment)) {
                for(Node child: children) {
                    if(child.getSegment().equals(segment)) {
                        answer = child;
                        break;
                    }
                }
            }
            return answer;
        }

        /**
         * Adds the given Node as child
         * @param child Node to be added as child which cannot be null
         * @return Child node which is either the given one or the one that is already added as child with that resource name
         */
        public Node addChild(Node child) {
            if(child == null) { throw new IllegalArgumentException("Cannot add undefined child"); }
            if(children == null) { children = new ArrayList<>(); }
            Node myChild = getChild(child.segment);
            if(myChild == null) {
                children.add(child);
                return child;
            } else {
                return myChild;
            }
        }

        /**
         * Creates a new child and adds it as child to this node as parent
         * @param segment Resource Name of the new child
         * @return Node that was created and added if not found otherwise the child with the given resource name
         */
        public Node addChild(String segment) {
            Node child = getChild(segment);
            if(child == null) {
                child = new Node(this, segment);
            }
            return child;
        }

        @Override
        public String toString() {
            return "Node(" + segment + ") {" + (children == null ? "" : children.toString()) + "}";
        }
    }
}
