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

import com.peregrine.sitemap.PrefixAndCutUrlExternalizerBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import java.util.Objects;
import java.util.Optional;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.commons.util.PerConstants.*;

@Component(service = { DefaultUrlExternalizer.class })
public final class DefaultUrlExternalizer extends PrefixAndCutUrlExternalizerBase {

    protected static final String _CONTENT_ = CONTENT_ROOT + SLASH;

    {
        setCutCount(3);
    }

    @Override
    public String getPrefix(final ResourceResolver resourceResolver, final String url) {
        if (!StringUtils.startsWith(url, _CONTENT_)) {
            return null;
        }

        final StringBuilder path = new StringBuilder(_CONTENT_);
        String name = StringUtils.substringAfter(url, _CONTENT_);
        name = StringUtils.substringBefore(name, SLASH);
        path.append(name);
        path.append(SLASH);
        path.append(TEMPLATES);
        path.append(SLASH);
        path.append(JCR_CONTENT);
        return Optional.ofNullable(resourceResolver)
                .map(rr -> rr.getResource(path.toString()))
                .filter(Objects::nonNull)
                .map(Resource::getValueMap)
                .map(m -> m.get(DOMAINS, String[].class))
                .filter(Objects::nonNull)
                .filter(s -> s.length > 0)
                .map(s -> s[0])
                .orElse(null);
    }
}
