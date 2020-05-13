package com.peregrine.adaption.impl;

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

import com.peregrine.adaption.Filter;
import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;

/**
 * Peregrine Wrapper Object for Pages
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
public class PerPageImpl extends PerBaseImpl implements PerPage {
    private static final AllFilter allFilter = new AllFilter();

    /** Reference to the Page Manager **/
    private PerPageManager pageManager;

    public PerPageImpl(Resource resource) {
        super(resource);
        this.pageManager = new PerPageManagerImpl(resource);
    }

    @Override
    public PerPageManager getPageManager() {
        return pageManager;
    }

    @Override
    public String getTitle() {
        return getContentProperty(JCR_TITLE, String.class);
    }

    @Override
    public Iterable<PerPage> listChildren() {
        return getChildren(allFilter, false);
    }

    @Override
    public Iterable<PerPage> listChildren(Filter<PerPage> filter) {
        return getChildren(filter, false);
    }

    @Override
    public Iterable<PerPage> listChildren(Filter<PerPage> filter, boolean deep) {
        return getChildren(filter, deep);
    }

    /**
     * Obtains a List of Page that matches the given Filter
     * 
     * @param filter Filter instance to select the desired pages
     * @param deep   If true the search goes deep
     * @return List of pages which can be empty but never null
     */
    private List<PerPage> getChildren(Filter<PerPage> filter, boolean deep) {
        List<PerPage> children = new ArrayList<PerPage>();
        for (Resource child : getResource().getChildren()) {
            // jcr:content nodes are converted to a PerPage of its parent so it is important
            // to exclude them
            // here otherwise the parent is added through the backdoor.
            if (!JCR_CONTENT.equals(child.getName())) {
                PerPage page = child.adaptTo(PerPage.class);
                if (page != null) {
                    if (filter.include(page)) {
                        children.add(page);
                        if (deep) {
                            children.addAll(getChildren(filter, deep));
                        }
                    }
                }
            }
        }
        return children;
    }

    @Override
    public boolean hasChild(String name) {
        Resource child = getResource().getChild(name);
        return child != null && child.adaptTo(PerPage.class) != null;
    }

    @Override
    public PerPage getParent() {
        Resource parent = getResource().getParent();
        return parent != null ? parent.adaptTo(PerPage.class) : null;
    }

    @Override
    public PerPage getTemplate() {
        PerPage answer = null;
        String templatePath = getContentProperty(TEMPLATE, String.class);
        if (templatePath != null) {
            answer = pageManager.getPage(templatePath);
        }
        return answer;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type.equals(PerPageManager.class)) {
            return (AdapterType) pageManager;
        } else {
            return super.adaptTo(type);
        }
    }

    @Override
    public PerPage getNext() {
        Resource resource = getResource();
        return findNext(resource, true);
    }

    /*
     * Pre-Order means that a direct child is returned first whereas Post-Order
     * means that first the furthers child is returned first
     *
     * Pre-Order: first child, sibling or parent sibling
     *
     * Post-Order: first leaf child, leaf sibling leaf child, leaf sibling or parent
     */
    private PerPage findNext(Resource resource, boolean preOrder) {
        PerPage answer = findNextChildPage(resource, null);
        if (answer == null) {
            Resource parent = resource.getParent();
            Resource child = resource;
            while (parent != null) {
                // Find any sibling in the parent this is after the this resource's path
                answer = findNextChildPage(parent, child);
                if (answer == null) {
                    child = parent;
                    parent = parent.getParent();
                    if (!isPrimaryType(parent, PAGE_PRIMARY_TYPE)) {
                        // The search ends at the first non-page node
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return answer;
    }

    /**
     * Looks for a child page of the given resource
     * 
     * @param resource Parent Resource to search for a page
     * @param after    If not null the returned page is the one that comes after
     *                 that resource There is not test that this is a child resource
     *                 of the given resource but if not it might lead to undesired
     *                 effects
     * @return The next child page if found otherwise null
     */
    private PerPage findNextChildPage(Resource resource, Resource after) {
        PerPage answer = null;
        boolean found = (after == null);
        for (Resource child : resource.getChildren()) {
            // JCR Content nodes will not yield a page -> ignore
            if (child.getName().equals(JCR_CONTENT)) {
                continue;
            }
            if (found) {
                if (isPrimaryType(child, PAGE_PRIMARY_TYPE)) {
                    answer = new PerPageImpl(child);
                    break;
                }
            } else {
                // 'found' can only be false if after is not null
                if (child.getName().equals(after.getName())) {
                    found = true;
                }
            }
        }
        return answer;
    }

    @Override
    public PerPage getPrevious() {
        Resource resource = getResource();
        return findPrevious(resource);
    }

    @Override
    public void markAsModified() {
        Resource resource = getResource();
        String user = resource.getResourceResolver().getUserID();
        Calendar now = Calendar.getInstance();
        // Update Content Properties
        ModifiableValueMap properties = getModifiableProperties();
        properties.put(JCR_LAST_MODIFIED_BY, user);
        properties.put(JCR_LAST_MODIFIED, now);
        // Update Page
        properties = resource.adaptTo(ModifiableValueMap.class);
        properties.put(JCR_LAST_MODIFIED_BY, user);
        properties.put(JCR_LAST_MODIFIED, now);
    }

    private Resource getLastChild(Resource res) {
        Iterable<Resource> children = res.getChildren();
        Resource answer = null;
        for(Resource child: children) {
            if(isPrimaryType(child, PAGE_PRIMARY_TYPE)) {
                answer = child;
            }
        }
        return answer;
    }


    /**
     * Finds the previous page in the page tree of the given page resource. This
     * will traverse the tree from right to left and bottom to top. Going from one
     * resource to the a previous one means we go to the bottom most node and
     * traverse back up.
     *
     * @param resource Starting Page Resource
     * @return Previous Page Wrapper Object if found
     */
    private PerPage findPrevious(Resource resource) {
        PerPage answer = findPreviousChildPage(resource.getParent(), resource);
        if (answer != null) {

            Resource child = answer.getResource();
            while(child != null) {
                child = getLastChild(child);
                if(child != null) {
                    answer = new PerPageImpl(child);
                }
            }
            return answer;
        } else {
            return new PerPageImpl(resource.getParent());
        }
        // if(answer == null) {
        //     Resource parent = resource.getParent();
        //     Resource current = resource;
        //     Iterable<Resource> children = parent.getChildren();
        //     Resource previous = null;
        //     for(Resource res: children) {
        //         if(!res.equals(current) && isPrimaryType(res, PAGE_PRIMARY_TYPE)) {
        //             previous = res;
        //         } else if(res.equals(current)) {
        //             break;
        //         }
        //     }

        //     // while(parent != null) {
        //     //     // Find any sibling in the parent this is before the this resource's path
        //     //     answer = findPreviousChildPage(parent, child);
        //     //     if(answer == null) {
        //     //         child = parent;
        //     //         parent = parent.getParent();
        //     //         if(!isPrimaryType(parent, PAGE_PRIMARY_TYPE)) {
        //     //             // The search ends at the first non-page node
        //     //             break;
        //     //         }
        //     //     } else {
        //     //         break;
        //     //     }
        //     // }
        // }

        // return answer;
    }

    /**
     * Look for a previous child page of the given resource before the given before sibiling
     * @param resource Parent Resource
     * @param before Sibling that is the next in the tree
     * @return Page Wrapper Object that comes before the given sibling
     */
    private PerPage findPreviousChildPage(Resource resource, Resource before) {
        Resource last = null;
        for(Resource child: resource.getChildren()) {
            // JCR Content nodes will not yield a page -> ignore
            if(child.getName().equals(JCR_CONTENT)) { continue; }
            if(before != null && child.getName().equals(before.getName())) {
                break;
            }
            if(isPrimaryType(child, PAGE_PRIMARY_TYPE)) {
                // Memorize child so that when the loops ends it is the resource that is last or before the 'before' resource
                last = child;
            }
        }
        return last != null ? new PerPageImpl(last) : null;
    }

    /**
     * Filter that includes all Pages
     */
    private static class AllFilter
        implements Filter<PerPage>
    {
        @Override
        public <T> boolean include(T t) {
            return true;
        }
    }
}
