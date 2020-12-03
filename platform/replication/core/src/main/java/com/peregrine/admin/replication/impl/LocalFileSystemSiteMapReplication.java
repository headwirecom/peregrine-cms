package com.peregrine.admin.replication.impl;

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

import com.peregrine.replication.Replication;
import com.peregrine.replication.Replication.ReplicationException;
import com.peregrine.replication.ReplicationsContainer;
import com.peregrine.sitemap.SiteMapFilesCache;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.commons.Chars.DOT;
import static java.util.Objects.isNull;

@Component(immediate = true)
public final class LocalFileSystemSiteMapReplication implements SiteMapFilesCache.RefreshListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ReplicationsContainer replicationsContainer;

    @Reference
    private SiteMapFilesCache siteMapFilesCache;

    @Reference
    private SiteMapUrlBuilder urlBuilder;

    @Activate
    public void activate() {
        siteMapFilesCache.addRefreshListener(this);
    }

    @Deactivate
    public void deactivate() {
        siteMapFilesCache.removeRefreshListener(this);
    }

    @Override
    public void onCacheRefreshed(final Resource rootPage, final String[] contents) {
        final Replication replication = replicationsContainer.getDefault();
        if (isNull(replication)) {
            return;
        }

        final Resource parent = rootPage.getParent();
        final String prefix = rootPage.getName() + DOT;
        for (int index = 0; index < contents.length; index++) {
            final String name = prefix + urlBuilder.getFileName(index);
            final String content = contents[index];
            try {
                replication.storeFile(parent, name, content);
            } catch (final ReplicationException e) {
                logger.warn(String.format("Could not replicate %s @ %s", name, rootPage.getPath()), e);
            }
        }
    }

}
