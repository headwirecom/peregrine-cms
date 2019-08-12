package com.peregrine.adaption;

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

/**
 * Peregrine Page with access to their data and parent / child pages.
 * It is adaptable to a Resource and Page Manager.
 *
 * Created by Andreas Schaefer on 6/2/17.
 */
public interface PerPage
    extends PerBase
{
    /** @return The Page Manager **/
    public PerPageManager getPageManager();
    /** @return Title of the Page if found otherwise null **/
    public String getTitle();
    /** @return List all children of type page **/
    public Iterable<PerPage> listChildren();
    /** 
     * @param filter Filter
     * @return List all children of type page that passes the filter.
     */
    public Iterable<PerPage> listChildren(Filter<PerPage> filter);
    /** 
     * @param filter Filter
     * @param deep <code>true</code> if this is recursive
     * @return List all children or their children of type page that passes the filter.
     */
    public Iterable<PerPage> listChildren(Filter<PerPage> filter, boolean deep);
    /** 
     * @param name Page name
     * @return If there is a child page with the given name.
     */
    public boolean hasChild(String name);
    /** @return Parent Page of this page if it is a page otherwise null **/
    public PerPage getParent();
    /** @return Template Page of this page if there is a template and is a page otherwise null **/
    public PerPage getTemplate();

    /** @return
     * The next Page which is the fist found (pre-order):
     * - first child if there are any children
     * - next sibling if there are any
     * - next sibling of a parent that has a child
     * - null if no page was found
     **/
    public PerPage getNext();

    /** @return
     * The previous Page which is the fist found (pre-order):
     * - last child if there are any children
     * - previous sibling if there are any
     * - previous sibling of a parent that has a child
     * - null if no page was found
     **/
    public PerPage getPrevious();

    /** Marks the given Page as modified **/
    public void markAsModified();
}
