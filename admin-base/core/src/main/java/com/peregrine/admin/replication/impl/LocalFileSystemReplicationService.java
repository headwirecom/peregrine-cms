package com.peregrine.admin.replication.impl;

/*-
 * #%L
 * admin base - Core
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

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import com.peregrine.admin.replication.ReplicationUtil;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.commons.util.PerUtil.MatchingResourceChecker;
import com.peregrine.commons.util.PerUtil.MissingOrOutdatedResourceChecker;
import com.peregrine.commons.util.PerUtil.ResourceChecker;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED_BY;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION_REF;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;

/**
 * This class replicates resources to a local file system folder
 * by exporting its content
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = Replication.class,
    immediate = true
)
@Designate(ocd = LocalFileSystemReplicationService.Configuration.class, factory = true)
public class LocalFileSystemReplicationService
    extends BaseFileReplicationService
{
    @ObjectClassDefinition(
        name = "Peregrine: Local FS Replication Service",
        description = "Each instance provides the configuration for a Local File System Replication"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Name",
            description = "Name of the Replication Service",
            defaultValue = "localFS",
            required = true
        )
        String name();
        @AttributeDefinition(
            name = "TargetFolder",
            description = "Path to the local folder where the content is exported to",
            required = true
        )
        String targetFolder();
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String name;
    private File targetFolder;

    private void setup(Configuration configuration) {
        name = configuration.name();
        if(name.isEmpty()) {
            throw new IllegalArgumentException("Replication Name cannot be empty");
        }
        String targetFolderPath = configuration.targetFolder();
        if(targetFolderPath.isEmpty()) {
            throw new IllegalArgumentException("Replication Target Folder cannot be empty");
        } else {
            File temp = new File(targetFolderPath);
            if(!temp.exists()) {
                throw new IllegalArgumentException("Replication Target Folder: '" + targetFolderPath + "' does not exist");
            } else if(!temp.isDirectory()) {
                throw new IllegalArgumentException("Replication Target Folder: '" + targetFolderPath + "' is not a directory");
            } else if(!temp.canRead() || !temp.canWrite()) {
                throw new IllegalArgumentException("Replication Target Folder: '" + targetFolderPath + "' cannot read or write");
            }
            targetFolder = temp;
        }
        log.trace("Local Replication Service Name: '{}' created with target folder: '{}'", name, targetFolder);
    }

    @Reference
    @SuppressWarnings("unused")
    private ReferenceLister referenceLister;
    @Reference
    @SuppressWarnings("unused")
    private SlingRequestProcessor requestProcessor;
    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;

    @Override
    public String getName() {
        return name;
    }

    @Override
    SlingRequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    @Override
    ReferenceLister getReferenceLister() {
        return referenceLister;
    }

    @Override
    boolean isFolderOnTarget(String path) {
        File check = new File(targetFolder, path);
        return check.exists();
    }

    @Override
    void createTargetFolder(String path) throws ReplicationException {
        File directory = targetFolder;
        String[] folders = path.split("/");
        for(String folder: folders) {
            if(folder != null && !folder.isEmpty()) {
                File newDirectory = new File(directory, folder);
                if(!newDirectory.exists()) {
                    if(!newDirectory.mkdir()) {
                        throw new ReplicationException("Failed to create folder: " + newDirectory.getAbsolutePath());
                    }
                }
                directory = newDirectory;
            }
        }
    }

    @Override
    void storeRendering(Resource resource, String extension, String content) throws ReplicationException {
        File directory = new File(targetFolder, resource.getParent().getPath());
        if(!directory.exists() || !directory.isDirectory()) {
            throw new ReplicationException("Failed to Store Rending as Parent Folder does not exist or is not a directory: " + directory.getAbsolutePath());
        }
        String fileName = resource.getName() + extension;
        File renderingFile = new File(directory, fileName);
        if(renderingFile.exists()) {
            if(renderingFile.isDirectory()) {
                throw new ReplicationException("Failed to Store Rending as target file is a directory: " + renderingFile.getAbsolutePath());
            } else {
                log.trace("Delete existing Rendering File: '{}'", renderingFile.getAbsolutePath());
                renderingFile.delete();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(renderingFile);
            fileWriter.append(content);
            fileWriter.close();
        } catch(IOException e) {
            throw new ReplicationException("Failed to write rending content to file: " + renderingFile.getAbsolutePath(), e);
        }
    }
}
