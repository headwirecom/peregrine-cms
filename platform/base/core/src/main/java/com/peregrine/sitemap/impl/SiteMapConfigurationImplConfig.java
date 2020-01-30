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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "%config.name", description = "%config.description")
public @interface SiteMapConfigurationImplConfig {

    @AttributeDefinition(name = "%enabled.name", description = "%enabled.description")
    boolean enabled() default false;

    @AttributeDefinition(name = "%pathRegex.name", description = "%pathRegex.description")
    String pathRegex();

    @AttributeDefinition(name = "%pageRecognizers.name", description = "%pageRecognizers.description")
    String[] pageRecognizers() default { };

    @AttributeDefinition(name = "%urlExternalizer.name", description = "%urlExternalizer.description")
    String urlExternalizer();

    @AttributeDefinition(name = "%xmlnsMappings.name", description = "%xmlnsMappings.description")
    String[] xmlnsMappings();

    @AttributeDefinition(name = "%propertyProviders.name", description = "%propertyProviders.description")
    String[] propertyProviders() default { };

    @AttributeDefinition(name = "%mandatoryCachedRootPaths.name", description = "%mandatoryCachedRootPaths.description")
    String[] mandatoryCachedRootPaths() default { };
}
