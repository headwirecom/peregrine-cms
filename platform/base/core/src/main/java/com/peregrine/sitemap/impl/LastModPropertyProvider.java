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
import com.peregrine.sitemap.PropertyProviderBase;
import org.osgi.service.component.annotations.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.peregrine.commons.util.PerConstants.ECMA_DATE_FORMAT;
import static com.peregrine.sitemap.SiteMapConstants.LAST_MOD;
import static java.util.Objects.nonNull;

@Component(service = { PropertyProvider.class, LastModPropertyProvider.class })
public final class LastModPropertyProvider extends PropertyProviderBase {

    private final DateFormat dateFormat = new SimpleDateFormat(ECMA_DATE_FORMAT);

    public LastModPropertyProvider() {
        super(LAST_MOD);
    }

    @Override
    public String extractValue(final Page page) {
        final Date lastModified = page.getLastModifiedDate();
        if (nonNull(lastModified)) {
            return dateFormat.format(lastModified);
        }

        return null;
    }

}
