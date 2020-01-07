package com.peregrine.sitemap.impl;

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

import com.peregrine.commons.Page;
import com.peregrine.sitemap.PropertyProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import static com.peregrine.commons.Strings.firstNotBlank;
import static java.util.Objects.nonNull;

@Component(service = { PropertyProvider.class }, immediate = true)
@Designate(ocd = DirectPropertyProviderConfig.class, factory = true)
public final class DirectPropertyProvider implements PropertyProvider {

    private String name;
    private String elementName;
    private String propertyName;

    @Activate
    public void activate(final DirectPropertyProviderConfig config) {
        name = firstNotBlank(config.name(), config.elementName(), config.propertyName());
        elementName = firstNotBlank(config.elementName(), config.propertyName(), config.name());
        propertyName = firstNotBlank(config.propertyName(), config.elementName(), config.name());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final String getPropertyName() {
        return elementName;
    }

    @Override
    public String extractValue(final Page page) {
        final Object value = page.getProperty(propertyName);
        return nonNull(value) ? value.toString() : null;
    }

}
