package com.peregrine.admin.replication.impl;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2019 headwire inc.
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

import com.peregrine.admin.replication.AbstractionReplicationService;
import com.peregrine.replication.ReferenceLister;
import com.peregrine.replication.Replication;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for issuing cache flush requests to mod_pagespeed on replication events.
 */
@Component(
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        service = Replication.class,
        immediate = true
)
@Designate(ocd = ModPageSpeedCacheInvalidationService.Configuration.class, factory = true)
public class ModPageSpeedCacheInvalidationService
        extends AbstractionReplicationService
{
    private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 5;
    
    @ObjectClassDefinition(
            name = "Peregrine: PageSpeed Cache Invalidation Service",
            description = "Each instance provides the configuration for a PageSpeed cache invalidation endpoint"
    )
    @interface Configuration {
        @AttributeDefinition(
                name = "Name",
                description = "Name of the PageSpeed cache invalidation service",
                required = true
        )
        String name();
        @AttributeDefinition(
                name = "Description",
                description = "Description of the PageSpeed cache invalidation service",
                required = true
        )
        String description();
        @AttributeDefinition(
                name = "PageSpeed cache invalidation endpoint",
                description = "Absolute URL to the PageSpeed cache invalidation endpoint (i.e. http://localhost/pagespeed_admin/cache).",
                required = true
        )
        String cacheInvalidationUrl();
    }
    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }


    private String cacheInvalidationUrl;

    private void setup(Configuration configuration) {
        init(configuration.name(), configuration.description());

        cacheInvalidationUrl = configuration.cacheInvalidationUrl();

        if(StringUtils.isBlank(cacheInvalidationUrl))
        {
            throw new IllegalArgumentException("Invalid PageSpeed cache invalidation URL: " + cacheInvalidationUrl);
        }

        log.trace("PageSpeed Invalidation Service Name: '{}' created", getName());
        log.trace("PageSpeed cache invalidation URL: '{}'", cacheInvalidationUrl);
    }

    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;
    @Reference
    @SuppressWarnings("unused")
    private ReferenceLister referenceLister;

    @Override
    public List<Resource> replicate(final Resource resource, boolean deep)
            throws ReplicationException
    {
        final String path = resource.getPath();

        // only issue a request for the top-level page. we don't care about jcr:content nodes as far as mod_pagespeed is concerned.
        if (StringUtils.isNotBlank(path) && !path.contains("jcr:content"))
        {
            final String cacheRequest = formatCacheKeyRequest(resource.getPath());
            if (StringUtils.isNoneBlank(cacheRequest))
            {
                invalidateCacheKey(cacheRequest);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<Resource> deactivate(Resource startingResource)
            throws ReplicationException
    {
        // TODO: call invalidateCacheKey()
        return Collections.emptyList();
    }

    @Override
    public List<Resource> replicate(List<Resource> resourceList) throws ReplicationException
    {
        // noop
        return Collections.emptyList();
    }

    /**
     * Formats a JCR path as suitable mod_pagespeed cache key.
     *
     * @param path the JCR path to format.
     *
     * @return An absolute URL for the cache invalidatio request on success, and <code>null</code> otherwise.
     */
    protected String formatCacheKeyRequest(final String path)
    {
        String cacheKey = null;
        try
        {
            String encodePath = new URLCodec().encode(path);
            return cacheInvalidationUrl + "?purge=" + encodePath + ".html";
        } catch (EncoderException e)
        {
            log.error("Error encoding JCR path: '{}'", path, e);
        }

        return cacheKey;
    }

    /**
     * Issues an HTTP request to the PageSpeed cache invalidation endpoint.
     *
     * @param url Absolute cache invalidation request URL for a given page in Peregrine.
     */
    protected void invalidateCacheKey(final String url)
    {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(HTTP_CLIENT_TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(HTTP_CLIENT_TIMEOUT_SECONDS * 1000).build();
        CloseableHttpClient httpClient =
                HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpGet httpGet = new HttpGet(url);
        try
        {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                log.info("PageSpeed cache invalidation request '{}' returned an '{}' response",
                        url, response.getStatusLine());
                HttpEntity entity1 = response.getEntity();
                EntityUtils.consume(entity1);
            } finally {
                response.close();
            }

        } catch (IOException e)
        {
            log.error("Error performing PageSpeed invalidation request: '{}'", url, e);
        }
    }
}
