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
import com.peregrine.commons.util.PerUtil;
import com.peregrine.sitemap.PageRecognizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.PER_REPLICATION_REF;

@Component(service = { PageRecognizer.class, PerPageRecognizer.class })
public final class PerPageRecognizer extends PerPageRecognizerBase {

    protected boolean isPageImpl(final Page candidate) {
        // would love to use
        // com.peregrine.admin.replication.ReplicationUtil.isReplicated
        // but this will introduce a cyclic dependency
        return Optional.ofNullable(candidate)
                .map(PerUtil::getProperJcrContent)
                .map(Resource::getValueMap)
                .map(m -> m.get(PER_REPLICATION_REF, String.class))
                .map(StringUtils::isNotBlank)
                .orElse(false);
    }

}
