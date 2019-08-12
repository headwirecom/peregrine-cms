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

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;

/**
 * Base Class for the Peregrine Wrapper Objects
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
public interface PerBase
    extends Adaptable
{
    /** @return Underlying resource which is never null **/
    public Resource getResource();
    /** @return Path of the underlying resource **/
    public String getPath();
    /** @return Name of the underlying resource **/
    public String getName();
    /** @return Last Modified Date of the resource if set otherwise null **/
    public Calendar getLastModified();
    /** @return User who Last Modified the resource if set otherwise null **/
    public String getLastModifiedBy();
    /** @return If the page has a content node **/
    public boolean hasContent();
    /** @return If the page is valid **/
    public boolean isValid();
    /** @return The Content Resource node if available otherwise null **/
    public Resource getContentResource();
    /** @return Value Map of the Content Resource if the page has a content **/
    public ValueMap getProperties();
    /** @return Modifiable Value Map of the Content Resource if the page has a content **/
    public ModifiableValueMap getModifiableProperties();

    /**
     * Provide Content Property
     * @param propertyName Name of the Property
     * @param type Response Type
     * @param <T> Type
     * @return Content Property Value converted to the given type or null if not found or could not convert
     */
    public <T> T getContentProperty(String propertyName, Class<T> type);

    /**
     * Provide Content Property
     * @param propertyName Name of the Property
     * @param defaultValue Value to be returned if not found and cannot be null
     * @param <T> Type
     * @return The content property of the type of the default value or if not found the default value instead
     */
    public <T> T getContentProperty(String propertyName, T defaultValue);
}
