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

import com.peregrine.render.RenderService;
import com.peregrine.replication.ReferenceLister;
import com.peregrine.replication.Replication;
import org.apache.commons.lang3.StringUtils;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.peregrine.admin.replication.ReplicationUtil.updateReplicationProperties;
import static com.peregrine.commons.Chars._SCORE;
import static com.peregrine.commons.ResourceUtils.jcrNameToFileName;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.getJcrContent;
import static com.peregrine.commons.util.PerUtil.intoList;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.splitIntoMap;
import static com.peregrine.commons.util.PerUtil.splitIntoProperties;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
    File createTargetFolder(final String path) throws ReplicationException {
        File answer = targetFolder;
        for (final String name: path.split(SLASH)) {
            if (StringUtils.isNotEmpty(name)) {
                answer = createTargetFolder(answer, jcrNameToFileName(name));
            }
        }

        return answer;
    }

    private File createTargetFolder(final File parent, final String name) throws ReplicationException {
        File answer = new File(parent, name);
        if (answer.exists() && !answer.isDirectory()) {
            // File exists but is not a folder (like an image or so) -> create a folder with '_' at the end
            answer = new File(parent, name + _SCORE);
        }

        if ((answer.exists() && answer.isDirectory()) || answer.mkdir()) {
            return answer;
        }

        throw new ReplicationException(String.format(FAILED_TO_CREATED_FOLDER, answer.getAbsolutePath()));
    }

    @Override
    List<ExportExtension> getExportExtensions() { return exportExtensions; }

    @Override
    List<String> getMandatoryRenditions() {
        return mandatoryRenditions;
    }

    @Override
    public String storeFile(final Resource parent, final String name, final String content) throws ReplicationException {
        return storeFile(parent, name, isNull(content) ? null : file -> {
            try (final FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.append(content);
            } catch (final IOException e) {
                return new ReplicationException(String.format(FAILED_TO_STORE_RENDERING, file.getAbsolutePath()), e);
            }

            return null;
        });
    }

    @Override
    public String storeFile(final Resource parent, final String name, final byte[] content) throws ReplicationException {
        return storeFile(parent, name, isNull(content) ? null : file -> {
            try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(content);
            } catch (final IOException e) {
                return new ReplicationException(String.format(CANNOT_WRITE_RENDERING, file.getAbsolutePath()), e);
            }

            return null;
        });
    }

    private String storeFile(final Resource parent, final String name, final Function<File, ReplicationException> fileConsumer)
            throws ReplicationException {
        if (isNull(fileConsumer)) {
            final File file = new File(targetFolder, parent.getPath() + SLASH + name);
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            return null;
        }

        final File file = createFileWithParentAndName(parent, name);
        final ReplicationException exception = fileConsumer.apply(file);
        if (nonNull(exception)) {
            throw exception;
        }

        final String localFileSystemPath = LOCAL_FILE_SYSTEM + file.getAbsolutePath();
        updateReplicationProperties(getJcrContent(parent), localFileSystemPath, null);
        return localFileSystemPath;
    }

    @Override
    void removeReplica(Resource resource, final List<Pattern> namePattern, final boolean isFolder) throws ReplicationException {
        final String resourceName = resource.getName();
        final File directory = new File(targetFolder, resource.getParent().getPath());
        if(!directory.exists() || !directory.isDirectory()) {
            throw new ReplicationException(String.format(FAILED_STORE_RENDERING_MISSING_PARENT_FOLDER, directory.getAbsolutePath()));
        }

        final File[] filesToBeDeletedFiles = directory.listFiles(file -> {
                    final String name = file.getName();
                    if (isFolder && file.isDirectory() && name.equals(resourceName)) {
                        return true;
                    }

                    if (isNull(namePattern)) {
                        return name.startsWith(resourceName);
                    }

                    for (final Pattern pattern : namePattern) {
                        return pattern.matcher(name).matches() && file.getName().startsWith(resourceName);
                    }

                    return false;
                }
        );
        if (isNull(filesToBeDeletedFiles)) {
            return;
        }

        for (final File toBeDeleted : filesToBeDeletedFiles) {
            log.trace("Delete File: '{}'", toBeDeleted.getAbsolutePath());
            if (!deleteFile(toBeDeleted)) {
                throw new ReplicationException(String.format(FAILED_TO_DELETE_FILE, toBeDeleted.getAbsolutePath()));
            }

            updateReplicationProperties(getJcrContent(resource), EMPTY, null);
        }
    }

    private boolean deleteFile(File file) {
        if(file.isDirectory()) {
            for(File child: file.listFiles()) {
                if(!deleteFile(child)) {
                    log.warn(String.format(FAILED_TO_DELETE_FILE, file.getAbsolutePath()));
                    return false;
                }
            }
        }

        return file.delete();
    }

    private File createFileWithParentAndName(final Resource parent, final String name) throws ReplicationException {
        final File directory = createTargetFolder(parent.getPath());
        if (!(directory.exists() && directory.isDirectory())) {
            throw new ReplicationException(String.format(FAILED_STORE_RENDERING_MISSING_PARENT_FOLDER, directory.getAbsolutePath()));
        }

        final File file = new File(directory, name);
        if (!file.exists()) {
            return file;
        }

        if (file.isDirectory()) {
            throw new ReplicationException(String.format(FAILED_STORE_RENDERING_FILE_IS_DIRECTORY, file.getAbsolutePath()));
        } else {
            log.trace("Delete existing Rendering File: '{}'", file.getAbsolutePath());
            file.delete();
        }

        return file;
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
