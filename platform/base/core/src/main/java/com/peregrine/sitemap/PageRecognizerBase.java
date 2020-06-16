package com.peregrine.sitemap;

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

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.isPropertyEqual;

public abstract class PageRecognizerBase implements PageRecognizer {

    private final String pagePrimaryType;
    private final String pageContentPrimaryType;
    private final String excludeFromSiteMapPropertyName;

    protected PageRecognizerBase(
            final String pagePrimaryType,
            final String pageContentPrimaryType,
            final String excludeFromSiteMapPropertyName) {
        this.pagePrimaryType = pagePrimaryType;
        this.pageContentPrimaryType = pageContentPrimaryType;
        this.excludeFromSiteMapPropertyName = excludeFromSiteMapPropertyName;
    }

    public final boolean isPage(final Page candidate) {
        if (!isPropertyEqual(candidate, JCR_PRIMARY_TYPE, pagePrimaryType)) {
            return false;
        }

        if (!candidate.hasContent()) {
            return false;
        }

        if (!isPropertyEqual(candidate.getContent(), JCR_PRIMARY_TYPE, pageContentPrimaryType)) {
            return false;
        }

        if (!candidate.containsProperty(SLING_RESOURCE_TYPE)) {
            return false;
        }

        if (candidate.containsProperty(excludeFromSiteMapPropertyName)) {
            Object excludeFromSiteMapProperty = candidate.getProperty(excludeFromSiteMapPropertyName);
            if (excludeFromSiteMapProperty instanceof String) {
                if (((String) excludeFromSiteMapProperty).equalsIgnoreCase("true")) {
                    return false;
                }
            } else if (excludeFromSiteMapProperty instanceof Boolean) {
                if (candidate.getProperty(excludeFromSiteMapPropertyName, false)) {
                    return false;
                }
            }

        }

        return isPageImpl(candidate);
    }

    protected abstract boolean isPageImpl(final Page candidate);

    public String getPagePrimaryType() {
        return pagePrimaryType;
    }

    public String getPageContentPrimaryType() {
        return pageContentPrimaryType;
    }

    public String getExcludeFromSiteMapPropertyName() {
        return excludeFromSiteMapPropertyName;
    }

}
