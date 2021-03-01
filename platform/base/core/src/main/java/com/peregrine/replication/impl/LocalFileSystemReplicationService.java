package com.peregrine.replication.impl;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.peregrine.commons.IOUtils.*;
import static com.peregrine.commons.TextUtils.replacePlaceholders;
import static com.peregrine.commons.Chars._SCORE;
import static com.peregrine.commons.ResourceUtils.jcrNameToFileName;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.*;
import static com.peregrine.replication.ReplicationUtil.markAsActivated;
import static com.peregrine.replication.ReplicationUtil.markAsDeactivated;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    extends FileReplicationServiceBase
{
    public static final int CREATE_NONE_STRATEGY = 0;
    public static final int CREATE_LEAF_STRATEGY = 1;
    public static final int CREATE_ALL_STRATEGY = 2;
    public static final String LOCAL_FILE_SYSTEM = "local-file-system://";
    public static final String FAILED_TO_CREATE_FOLDER = "Failed to create folder '%s' under '%s'";
    public static final String FAILED_STORE_RENDERING_MISSING_PARENT_FOLDER = "Failed to Store Rendering as Parent Folder does not exist or is not a directory: '%s'";
    public static final String FAILED_STORE_RENDERING_FILE_IS_DIRECTORY = "Failed to Store Rendering as target file is a directory:: '%s'";
    public static final String FAILED_TO_STORE_RENDERING = "Failed to write raw rending content to file: '%s'";
    public static final String SUPPORTED_TYPES_EMPTY = "Supported Types is empty for Extension: '%s'";
    public static final String COULD_NOT_CREATE_LEAF_FOLDER = "Could not create leaf folder: '%s'";
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
            defaultValue = "localFS"
        )
        String name();
        @AttributeDefinition(
            name = "Description",
            description = "Description of this Replication Service"
        )
        String description();
        @AttributeDefinition(
            name = "TargetFolder",
            description = "Path to the local folder where the content is exported to"
        )
        String targetFolder();
        @AttributeDefinition(
            name = "Creation Strategy",
            description = "Indicates what to create for the Target Folder. 0 (or any other not mentioned number) means no creation, 1 means creating only the leaf folder, 2 means creating all missing folders",
            defaultValue = CREATE_NONE_STRATEGY + "",
            min = CREATE_NONE_STRATEGY + "",
            max = CREATE_ALL_STRATEGY + ""
        )
        int creationStrategy();
        @AttributeDefinition(
            name = "Export Extensions",
            description = "List of Export Extension in the format of <extension>=<comma separated list of primary types>"
        )
        String[] exportExtensions();
        @AttributeDefinition(
            name = "Mandatory Renditions",
            description = "List of all the required renditions that are replicated (if missing they are created)"
        )
        String[] mandatoryRenditions();
    }

    private File targetFolder;
    private final List<ExportExtension> exportExtensions = new ArrayList<>();
    private List<String> mandatoryRenditions = new ArrayList<>();

    @Activate
    @SuppressWarnings("unused")
    void activate(BundleContext context, Configuration configuration) { setup(context, configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(BundleContext context, Configuration configuration) { setup(context, configuration); }

    private void setup(BundleContext context, Configuration configuration) {
        log.trace("Create Local FS Replication Service Name: '{}'", configuration.name());
        init(configuration.name(), configuration.description());
        log.debug("Extension: '{}'", configuration.exportExtensions());
        int creationStrategy = configuration.creationStrategy();
        exportExtensions.clear();
        Map<String, List<String>> extensions = splitIntoMap(configuration.exportExtensions(), "=", "\\|");
        for(Entry<String, List<String>> extension: extensions.entrySet()) {
            String name = extension.getKey();
            if(isNotEmpty(name)) {
                List<String> types = extension.getValue();
                if(types != null && !types.isEmpty()) {
                    exportExtensions.add(new ExportExtension(name, types));
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
            targetFolderPath = replacePlaceholders(targetFolderPath, context::getProperty);
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
            if (nonNull(answer) && StringUtils.isNotEmpty(name)) {
                final String fileName = jcrNameToFileName(name);
                answer = createChildDirectory(answer, fileName, fileName + _SCORE);
            }
        }

        if (isNull(answer)) {
            throw new ReplicationException(String.format(FAILED_TO_CREATE_FOLDER, path, targetFolder.getAbsolutePath()));
        }

        return answer;
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
        markAsActivated(getJcrContent(parent), localFileSystemPath);
        return localFileSystemPath;
    }

    @Override
    void removeReplica(Resource resource, final Pattern namePattern, final boolean isFolder) throws ReplicationException {
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

                    return name.startsWith(resourceName) && (isNull(namePattern) || namePattern.matcher(name).matches());
                }
        );
        if (isNull(filesToBeDeletedFiles)) {
            return;
        }

        for (final File toBeDeleted : filesToBeDeletedFiles) {
            log.trace("Delete File: '{}'", toBeDeleted.getAbsolutePath());
            if (!deleteFileOrDirectory(toBeDeleted)) {
                throw new ReplicationException(String.format(FAILED_TO_DELETE_FILE, toBeDeleted.getAbsolutePath()));
            }

            markAsDeactivated(getJcrContentOrSelf(resource));
        }
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

}
