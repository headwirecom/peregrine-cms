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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.framework.BundleContext;
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.peregrine.commons.util.PerUtil.intoList;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.splitIntoMap;

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
    public static final int CREATE_NONE_STRATEGY = 0;
    public static final int CREATE_LEAF_STRATEGY = 1;
    public static final int CREATE_ALL_STRATEGY = 2;

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
            name = "Description",
            description = "Description of this Replication Service",
            required = true
        )
        String description();
        @AttributeDefinition(
            name = "TargetFolder",
            description = "Path to the local folder where the content is exported to",
            required = true
        )
        String targetFolder();
        @AttributeDefinition(
            name = "Creation Strategy",
            description = "Indicates what to create for the Target Folder. 0 (or any other not mentioned number) means no creation, 1 means creating only the leaf folder, 2 means creating all missing folders",
            defaultValue = CREATE_NONE_STRATEGY + "",
            min = CREATE_NONE_STRATEGY + "",
            max = CREATE_ALL_STRATEGY + "",
            required = true
        )
        int creationStrategy();
        @AttributeDefinition(
            name = "Export Extensions",
            description = "List of Export Extension in the format of <extension>=<comma separated list of primary types>",
            required = true
        )
        String[] exportExtensions();
        @AttributeDefinition(
            name = "Mandatory Renditions",
            description = "List of all the required renditions that are replicated (if missing they are created)",
            required = true
        )
        String[] mandatoryRenditions();
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(BundleContext context, Configuration configuration) { setup(context, configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(BundleContext context, Configuration configuration) { setup(context, configuration); }

    private File targetFolder;
    private int creationStrategy = CREATE_NONE_STRATEGY;
    private Map<String, List<String>> exportExtensions = new HashMap<>();
    private List<String> mandatoryRenditions = new ArrayList<>();

    private void setup(BundleContext context, Configuration configuration) {
        log.trace("Create Local FS Replication Service Name: '{}'", configuration.name());
        init(configuration.name(), configuration.description());
        creationStrategy = configuration.creationStrategy();
        exportExtensions = splitIntoMap(configuration.exportExtensions(), "=", "\\|");
        mandatoryRenditions = intoList(configuration.mandatoryRenditions());
        String targetFolderPath = configuration.targetFolder();
        if(targetFolderPath.isEmpty()) {
            throw new IllegalArgumentException("Replication Target Folder cannot be empty");
        } else {
            targetFolderPath = handlePlaceholders(context, targetFolderPath);
            File temp = new File(targetFolderPath);
            if(!temp.exists()) {
                switch(creationStrategy) {
                    case CREATE_LEAF_STRATEGY:
                        if(!temp.mkdir()) {
                            throw new IllegalArgumentException("Could not create leaf folder: " + temp.getAbsolutePath());
                        }
                        break;
                    case CREATE_ALL_STRATEGY:
                        if(!temp.mkdirs()) {
                            throw new IllegalArgumentException("Could not create all folders: " + temp.getAbsolutePath());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Replication Target Folder: '" + targetFolderPath + "' does not exist and will not be created");
                }
            } else if(!temp.isDirectory()) {
                throw new IllegalArgumentException("Replication Target Folder: '" + targetFolderPath + "' is not a directory");
            } else if(!temp.canRead() || !temp.canWrite()) {
                throw new IllegalArgumentException("Replication Target Folder: '" + targetFolderPath + "' cannot read or write");
            }
            targetFolder = temp;
        }
        log.trace("Local FS Replication Service Name: '{}' created with target folder: '{}'", getName(), targetFolder);
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
    Map<String, List<String>> getExportExtensions() { return exportExtensions; }

    @Override
    List<String> getMandatoryRenditions() {
        return mandatoryRenditions;
    }

    @Override
    String storeRendering(Resource resource, String extension, String content) throws ReplicationException {
        File renderingFile = createRenderingFile(resource, extension);
        try {
            FileWriter fileWriter = new FileWriter(renderingFile);
            fileWriter.append(content);
            fileWriter.close();
        } catch(IOException e) {
            throw new ReplicationException("Failed to write rending content to file: " + renderingFile.getAbsolutePath(), e);
        }
        return "local-file-system://" + renderingFile.getAbsolutePath();
    }

    @Override
    String storeRendering(Resource resource, String extension, byte[] content) throws ReplicationException {
        File renderingFile = createRenderingFile(resource, extension);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(renderingFile);
            fileOutputStream.write(content);
            fileOutputStream.close();
        } catch(IOException e) {
            throw new ReplicationException("Failed to write raw rending content to file: " + renderingFile.getAbsolutePath(), e);
        }
        return "local-file-system://" + renderingFile.getAbsolutePath();
    }

    @Override
    void removeReplica(Resource resource, final List<Pattern> namePattern, final boolean isFolder) throws ReplicationException {
        final String resourceName = resource.getName();
        File directory = new File(targetFolder, resource.getParent().getPath());
        if(!directory.exists() || !directory.isDirectory()) {
            throw new ReplicationException("Failed to Store Rending as Parent Folder does not exist or is not a directory: " + directory.getAbsolutePath());
        }
        final List<Pattern> patterns = new ArrayList<>();
        File[] filesToBeDeletedFiles = directory.listFiles(
            new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if(isFolder) {
                        if(file.isDirectory() && file.getName().equals(resourceName)) {
                            return true;
                        }
                    }
                    if(namePattern == null) {
                        return file.getName().startsWith(resourceName);
                    } else {
                        for(Pattern pattern : namePattern) {
                            if(pattern.matcher(file.getName()).matches()) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
        );
        if(filesToBeDeletedFiles != null) {
            for(File toBeDeleted : filesToBeDeletedFiles) {
                log.trace("Delete File: '{}'", toBeDeleted.getAbsolutePath());
                if(!deleteFile(toBeDeleted)) {
                    throw new ReplicationException("Failed to delete file: " + toBeDeleted.getAbsolutePath());
                }

            }
        }
    }

    private boolean deleteFile(File file) {
        if(file.isDirectory()) {
            for(File child: file.listFiles()) {
                boolean answer = deleteFile(child);
                if(!answer) {
                    log.warn("Failed to delete file: '{}'", file.getAbsolutePath());
                    return answer;
                }
            }
        }
        return file.delete();
    }

    private File createRenderingFile(Resource resource, String extension) throws ReplicationException {
        File directory = new File(targetFolder, resource.getParent().getPath());
        if(!directory.exists() || !directory.isDirectory()) {
            throw new ReplicationException("Failed to Store Rending as Parent Folder does not exist or is not a directory: " + directory.getAbsolutePath());
        }
        String fileName = resource.getName() + (isNotEmpty(extension) ? "." + extension : "");
        File renderingFile = new File(directory, fileName);
        if(renderingFile.exists()) {
            if(renderingFile.isDirectory()) {
                throw new ReplicationException("Failed to Store Rending as target file is a directory: " + renderingFile.getAbsolutePath());
            } else {
                log.trace("Delete existing Rendering File: '{}'", renderingFile.getAbsolutePath());
                renderingFile.delete();
            }
        }
        return renderingFile;
    }

    public static final String PLACEHOLDER_START_TOKEN = "${";
    public static final String PLACEHOLDER_END_TOKEN = "}";

    private String handlePlaceholders(BundleContext context, String source) {
        log.trace("System Properties: '{}'", System.getProperties());
        String answer = source;
        log.trace("Handle Place Holder: '{}'", source);
        while(true) {
            int startIndex = answer.indexOf(PLACEHOLDER_START_TOKEN);
            log.trace("Handle Place Holder, start index; '{}'", startIndex);
            if(startIndex >= 0) {
                int endIndex = answer.indexOf(PLACEHOLDER_END_TOKEN, startIndex);
                log.trace("Handle Place Holder, end index; '{}'", endIndex);
                if(endIndex >= 0) {
                    String placeHolderName = answer.substring(startIndex + PLACEHOLDER_START_TOKEN.length(), endIndex);
                    String value = System.getProperty(placeHolderName);
                    log.trace("Placeholder found: '{}', property value: '{}'", placeHolderName, value);
                    if(value == null) {
                        value = context.getProperty(placeHolderName);
                        log.trace("Placeholder found through bundle context: '{}', property value: '{}'", placeHolderName, value);
                    }
                    if(value != null) {
                        answer = answer.substring(0, startIndex) + value +
                            (answer.length() - 1 > endIndex ? answer.substring(endIndex + 1) : "");
                    } else {
                        throw new IllegalArgumentException("Place Holder: '" + placeHolderName + "' did not yield a value");
                    }
                } else {
                    throw new IllegalArgumentException("Place Holder String opened a Place Holder with '" + PLACEHOLDER_START_TOKEN + "' but did not close it with: '" + PLACEHOLDER_END_TOKEN + "'");
                }
            } else {
                // Done -> exit
                break;
            }
        }
        log.trace("Place Holder handled, return: '{}'", answer);
        return answer;
    }
}
