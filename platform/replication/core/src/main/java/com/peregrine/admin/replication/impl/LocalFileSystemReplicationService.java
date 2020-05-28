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

import com.peregrine.replication.ReferenceLister;
import com.peregrine.replication.Replication;
import com.peregrine.render.RenderService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.peregrine.commons.util.PerUtil.intoList;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.splitIntoMap;
import static com.peregrine.commons.util.PerUtil.splitIntoProperties;

/**
 * This class replicates resources to a local file system folder
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
    public static final String LOCAL_FILE_SYSTEM = "local-file-system://";
    public static final String FAILED_TO_CREATED_FOLDER = "Failed to create folder: '%s'";
    public static final String FAILED_TO_DELETE_FILE = "Failed to delete file: '%s'";
    public static final String FAILED_STORE_RENDERING_MISSING_PARENT_FOLDER = "Failed to Store Rendering as Parent Folder does not exist or is not a directory: '%s'";
    public static final String FAILED_STORE_RENDERING_FILE_IS_DIRECTORY = "Failed to Store Rendering as target file is a directory:: '%s'";
    public static final String PLACEHOLDER_NO_VALUE = "Place Holder: '%s' did not yield a value";
    public static final String PLACEHOLDER_UNMATCHED_SEPARATORS = "Place Holder String opened a Place Holder with '%s' but did not close it with: '%s'";
    public static final String FAILED_TO_STORE_RENDERING = "Failed to write raw rending content to file: '%s'";
    public static final String SUPPORTED_TYPES_EMPTY = "Supported Types is empty for Extension: '%s'";
    public static final String COULD_NOT_CREATE_LEAF_FOLDER = "Could not create leaf folder: '%s'";
    public static final String EXPORT_FOLDER = "exportFolder";
    public static final String REPLICATION_TARGET_FOLDER_CANNOT_BE_EMPTY = "Replication Target Folder cannot be empty";
    public static final String COULD_NOT_CREATE_ALL_FOLDERS = "Could not create all folders: '%s'";
    public static final String REPLICATION_FOLDER_NOT_CREATED = "Replication Target Folder: '%s' does not exist and will not be created";
    public static final String REPLICATION_FOLDER_NO_DIRECTORY = "Replication Target Folder: '%s' is not a directory";
    public static final String REPLICATION_FOLDER_NO_WRITE = "Replication Target Folder: '%s' cannot read or write";
    public static final String CANNOT_WRITE_RENDERING = "Failed to write raw rending content to file: '%s'";

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
    private List<ExportExtension> exportExtensions = new ArrayList<>();
    private List<String> mandatoryRenditions = new ArrayList<>();

    private void setup(BundleContext context, Configuration configuration) {
        log.trace("Create Local FS Replication Service Name: '{}'", configuration.name());
        init(configuration.name(), configuration.description());
        log.debug("Extension: '{}'", configuration.exportExtensions());
        creationStrategy = configuration.creationStrategy();
        exportExtensions.clear();
        Map<String, List<String>> extensions = splitIntoMap(configuration.exportExtensions(), "=", "\\|");
        Map<String, List<String>> extensionParameters = new HashMap<>();
        for(Entry<String, List<String>> extension: extensions.entrySet()) {
            String name = extension.getKey();
            if(isNotEmpty(name)) {
                List<String> types = extension.getValue();
                if(types != null && !types.isEmpty()) {
                    List<String> parameters = extensionParameters.get(name);
                    boolean exportFolder = false;
                    if(parameters != null) {
                        String param = splitIntoProperties(parameters, ":").get(EXPORT_FOLDER) + "";
                        exportFolder = Boolean.TRUE.toString().equalsIgnoreCase(param);
                    }
                    exportExtensions.add(
                        new ExportExtension(name, types).setExportFolders(exportFolder)
                    );
                } else {
                    throw new IllegalArgumentException(String.format(SUPPORTED_TYPES_EMPTY, extension));
                }
            } else {
                log.warn("Configuration contained an empty extension");
            }
        }
        log.debug("Mandatory Renditions: '{}'", configuration.mandatoryRenditions());
        mandatoryRenditions = intoList(configuration.mandatoryRenditions());
        String targetFolderPath = configuration.targetFolder();
        if(targetFolderPath.isEmpty()) {
            throw new IllegalArgumentException(REPLICATION_TARGET_FOLDER_CANNOT_BE_EMPTY);
        } else {
            targetFolderPath = handlePlaceholders(context, targetFolderPath);
            log.trace("Target Folder Path: '{}', creation strategy: '{}'", targetFolderPath, creationStrategy);
            File temp = new File(targetFolderPath);
            if(!temp.exists()) {
                switch(creationStrategy) {
                    case CREATE_LEAF_STRATEGY:
                        if(!temp.mkdir()) {
                            throw new IllegalArgumentException(String.format(COULD_NOT_CREATE_LEAF_FOLDER, temp.getAbsolutePath()));
                        }
                        break;
                    case CREATE_ALL_STRATEGY:
                        if(!temp.mkdirs()) {
                            throw new IllegalArgumentException(COULD_NOT_CREATE_ALL_FOLDERS + temp.getAbsolutePath());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(String.format(REPLICATION_FOLDER_NOT_CREATED, targetFolderPath));
                }
            } else if(!temp.isDirectory()) {
                throw new IllegalArgumentException(String.format(REPLICATION_FOLDER_NO_DIRECTORY, targetFolderPath));
            } else if(!temp.canRead() || !temp.canWrite()) {
                throw new IllegalArgumentException(String.format(REPLICATION_FOLDER_NO_WRITE, targetFolderPath));
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
    private RenderService renderService;
    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;

    @Override
    RenderService getRenderService() {
        return renderService;
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
    File createTargetFolder(String path) throws ReplicationException {
        File directory = targetFolder;
        String[] folders = path.split("/");
        for(String folder: folders) {
            if(folder != null && !folder.isEmpty()) {
                File newDirectory = new File(directory, folder);
                if(!newDirectory.exists()) {
                    if(!newDirectory.mkdir()) {
                        throw new ReplicationException(String.format(FAILED_TO_CREATED_FOLDER, newDirectory.getAbsolutePath()));
                    }
                } else if(!newDirectory.isDirectory()) {
                    // File exists but is not a folder (like an image or so) -> create a folder with '_' at the end
                    String addendum = folder + "_";
                    newDirectory = new File(directory, addendum);
                    if(!newDirectory.exists()) {
                        if (!newDirectory.mkdir()) {
                            throw new ReplicationException(String.format(FAILED_TO_CREATED_FOLDER, newDirectory.getAbsolutePath()));
                        }
                    }
                }
                directory = newDirectory;
            }
        }
        return directory;
    }

    @Override
    List<ExportExtension> getExportExtensions() { return exportExtensions; }

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
            throw new ReplicationException(String.format(FAILED_TO_STORE_RENDERING, renderingFile.getAbsolutePath()), e);
        }
        return LOCAL_FILE_SYSTEM + renderingFile.getAbsolutePath();
    }

    @Override
    String storeRendering(Resource resource, String extension, byte[] content) throws ReplicationException {
        File renderingFile = createRenderingFile(resource, extension);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(renderingFile);
            fileOutputStream.write(content);
            fileOutputStream.close();
        } catch(IOException e) {
            throw new ReplicationException(String.format(CANNOT_WRITE_RENDERING, renderingFile.getAbsolutePath()), e);
        }
        return LOCAL_FILE_SYSTEM + renderingFile.getAbsolutePath();
    }

    @Override
    void removeReplica(Resource resource, final List<Pattern> namePattern, final boolean isFolder) throws ReplicationException {
        final String resourceName = resource.getName();
        File directory = new File(targetFolder, resource.getParent().getPath());
        if(!directory.exists() || !directory.isDirectory()) {
            throw new ReplicationException(String.format(FAILED_STORE_RENDERING_MISSING_PARENT_FOLDER, directory.getAbsolutePath()));
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
                    throw new ReplicationException(String.format(FAILED_TO_DELETE_FILE, toBeDeleted.getAbsolutePath()));
                }

            }
        }
    }

    private boolean deleteFile(File file) {
        if(file.isDirectory()) {
            for(File child: file.listFiles()) {
                boolean answer = deleteFile(child);
                if(!answer) {
                    log.warn(String.format(FAILED_TO_DELETE_FILE, file.getAbsolutePath()));
                    return answer;
                }
            }
        }
        return file.delete();
    }

    private File createRenderingFile(Resource resource, String extension) throws ReplicationException {
        File directory = createTargetFolder(resource.getParent().getPath());
        if(!directory.exists() || !directory.isDirectory()) {
            throw new ReplicationException(String.format(FAILED_STORE_RENDERING_MISSING_PARENT_FOLDER, directory.getAbsolutePath()));
        }
        String fileName = resource.getName() + (isNotEmpty(extension) ? "." + extension : "");
        File renderingFile = new File(directory, fileName);
        if(renderingFile.exists()) {
            if(renderingFile.isDirectory()) {
                throw new ReplicationException(String.format(FAILED_STORE_RENDERING_FILE_IS_DIRECTORY, renderingFile.getAbsolutePath()));
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
                        throw new IllegalArgumentException(String.format(PLACEHOLDER_NO_VALUE, placeHolderName));
                    }
                } else {
                    throw new IllegalArgumentException(String.format(PLACEHOLDER_UNMATCHED_SEPARATORS, PLACEHOLDER_START_TOKEN, PLACEHOLDER_END_TOKEN));
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
