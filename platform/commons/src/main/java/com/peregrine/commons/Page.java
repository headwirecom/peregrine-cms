package com.peregrine.commons;

/*-
 * #%L
 * platform base - Core
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

import com.peregrine.commons.util.PerConstants;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;
import java.util.Date;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class Page extends ResourceWrapper {

    private final ValueMap properties;
    private final Resource content;
    private final ValueMap contentProperties;

    public Page(final Resource resource) {
        super(resource);
        properties = resource.getValueMap();
        content = resource.getChild(PerConstants.JCR_CONTENT);
        contentProperties = isNull(content) ? null : content.getValueMap();
    }

    public boolean hasContent() {
        return nonNull(content);
    }

    public Resource getContent() {
        return content;
    }

    public boolean containsProperty(final String name) {
        if (nonNull(contentProperties) && contentProperties.containsKey(name)) {
            return true;
        }

        return properties.containsKey(name);
    }

    public Object getProperty(final String name) {
        if (nonNull(contentProperties) && contentProperties.containsKey(name)) {
            return contentProperties.get(name);
        }

        return properties.get(name);
    }

    public <Type> Type getProperty(final String name, final Class<Type> type) {
        final Object value = getProperty(name);
        return isNull(value) ? null : type.cast(value);
    }

    @SuppressWarnings("unchecked")
	  public <Type> Type getProperty(final String name, final Type defaultValue) {
        if (!containsProperty(name)) {
            return defaultValue;
        }

        final Object value = getProperty(name);
        return isNull(value) ? null : (Type) value;
    }

    public Calendar getLastModified() {
        final Calendar calendar = getProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.class);
        return isNull(calendar) ? getProperty(JcrConstants.JCR_CREATED, Calendar.class) : calendar;
    }

    public Date getLastModifiedDate() {
        final Calendar calendar = getLastModified();
        return isNull(calendar) ? null : calendar.getTime();
    }
}
