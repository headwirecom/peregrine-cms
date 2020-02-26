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

import com.peregrine.sitemap.HasName;
import com.peregrine.sitemap.NamedServiceRetriever;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(service = NamedServiceRetriever.class)
public final class NamedServiceRetrieverImpl implements NamedServiceRetriever {

    private BundleContext context;

    @Activate
    public void activate(final BundleContext context) {
        this.context = context;
    }

    @Override
    public <Service extends HasName> Service getNamedService(final Class<Service> clazz, final String name) {
        try {
            for (final ServiceReference<Service> reference : context.getServiceReferences(clazz, null)) {
                final Service service = context.getService(reference);
                if (StringUtils.equals(name, service.getName())) {
                    return service;
                } else {
                    context.ungetService(reference);
                }
            }
        } catch (final InvalidSyntaxException e) {
            return null;
        }

        return null;
    }
}
