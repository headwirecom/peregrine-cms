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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern ROOT_SITE_PATH_PATTERN = Pattern.compile("^(/content/[a-zA-Z0-9_]+)/.*$");
    
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
        // nooop
        return Collections.emptyList();
    }

    @Override
    public List<Resource> deactivate(Resource startingResource)
            throws ReplicationException
    {
        return Collections.emptyList();
    }

    @Override
    public List<Resource> replicate(List<Resource> resourceList) throws ReplicationException
    {
        if (StringUtils.isNotBlank(cacheInvalidationUrl))
        {
            // If an invalidation URL is specified in the OSGi config, use that URL for invalidation. This will
            // preserve backwards compatibility.
            invalidateCacheKey(cacheInvalidationUrl);
        } else {
            // If an invalidation URL is not specified in the OSGi config, attempt to lookup all the domains
            // associated with the replicated node/site.
            for (String siteInvalidationUrl : getSitesInvalidationUrls(resourceList))
            {
                invalidateCacheKey(siteInvalidationUrl);
            }
        }

        return Collections.emptyList();
    }

    private Set<String> getSitesInvalidationUrls(final List<Resource> resources)
    {
        Set<String> siteInvalidationUls = new HashSet<>();
        Set<String> sites = new HashSet<String>();
        if (resources != null)
        {
            for (final Resource resource: resources)
            {
                final String sitePath = getSitePath(resource);
                if (sites.contains(sitePath))
                {
                    log.trace("Skipping... unique site already processed");
                    continue;
                }
                sites.add(sitePath);

                Set<String> domains = getDomains(sitePath, resource);
                for (String domain: domains)
                {
                    // construct site-wide pagespeed invalidation request URL for a given vhost
                    siteInvalidationUls.add(domain + "/*");
                }

            }
        }
        return siteInvalidationUls;
    }

    /**
     * Extracts the root site path for a given resource.
     *
     * @param resource
     * @return A site path (i.e. /content/site) on success, and <code>null</code> otherwise.
     */
    private String getSitePath(final Resource resource)
    {
        String sitePath = null;

        Matcher matcher = ROOT_SITE_PATH_PATTERN.matcher(resource.getPath());
        if (matcher.matches() && matcher.groupCount() == 1)
        {
            sitePath = matcher.group(1);
        }

        return sitePath;
    }

    /**
     * Find domains associated to the given site.
     *
     * @param sitePath Site path (i.e. /content/site)
     * @param resource Any valid, non-null resource
     * @return A set of domains if they exist for the site, otherwise and empty set.
     */
    private Set<String> getDomains(final String sitePath, final Resource resource)
    {
        Set<String> domains = new HashSet<>();

        final String rootTemplate = sitePath + "/templates/jcr:content";
        final Resource rootTemplateNode = resource.getResourceResolver().getResource(rootTemplate);

        if (null == rootTemplateNode)
        {
            log.warn("Root template is null. Can't get domains for site: '{}'", sitePath);
            return domains;
        }

        final ValueMap properties = rootTemplateNode.getValueMap();

        if ( properties != null && properties.containsKey("domains"))
        {
            String[] vals = properties.get("domains", String[].class);
            if (vals != null && vals.length > 0 )
            {
                for (int i = 0; i < vals.length; i++)
                {
                    domains.add(vals[i]);
                }
            }
        }

        return domains;
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

        HttpPurge httpPurge = new HttpPurge(url);
        try
        {
            CloseableHttpResponse response = httpClient.execute(httpPurge);
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

    public class HttpPurge extends HttpRequestBase
    {
        public HttpPurge()
        {
            super();
        }

        public HttpPurge(final String url)
        {
            super();
            setURI(URI.create(url));
        }

        @Override
        public String getMethod()
        {
            return "PURGE";
        }
    }
}
