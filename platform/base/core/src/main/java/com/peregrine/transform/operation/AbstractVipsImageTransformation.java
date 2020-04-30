package com.peregrine.transform.operation;

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

import com.peregrine.process.ExternalProcessException;
import com.peregrine.process.ProcessContext;
import com.peregrine.process.ProcessRunner;
import com.peregrine.transform.ImageContext;
import com.peregrine.transform.ImageTransformation;
import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.isEmpty;

/**
 * Base Class for VIPS Image Transformation made by
 * calling VIPS as external process
 *
 * Created by Andreas Schaefer on 5/19/17.
 */
public abstract class AbstractVipsImageTransformation
    implements ImageTransformation
{
    public static final String VIPS = "vips";
    public static final String VERSION_PARAMETER = "--version";
    public static final String IMAGE_CONTEXT_MUST_BE_DEFINED_FOR_TRANSFORMATION = "Image Context must be defined for Transformation";
    public static final String VIPS_OPERATION_NAME_CANNOT_BE_EMPTY = "VIPS Operation Name cannot be empty";
    public static final String COULD_NOT_CREATE_INPUT_FILE = "Could not create input file: ";
    public static final String COULD_NOT_CREATE_OUTPUT_FILE = "Could not create output file: ";
    public static final String INPUT_WAS_NOT_USED_IN = "Input was not used ({in})";
    public static final String FAILED_TO_EXECUTE_VIPS_OPERATION = "Failed to execute VIPS Operation: ";
    public static final String COULD_NOT_CREATE_TEMPORARY_FOLDER = "Could not create temporary folder: ";
    public static final String FAILED_TO_EXECUTE_VIPS_OPERATION_WITH_CODE = "Failed to execute VIPS Operation: '%s' with exit code: '%s'";
    public static final String IN_TOKEN = "{in}";
    public static final String OUT_TOKEN = "{out}";

    public static final String TRANSFORMATION_NAME_CANNOT_BE_EMPTY = "Transformation Name cannot be empty";
    public static final String TRANSFORMATION_WIDTH_MUST_BE_PROVIDED = "Transformation Width must be greater than 0";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private boolean enabled = false;

    private boolean vipsInstalled = false;
    private long lastCheckTime = -1;
    private long checkTimeout = 5 * 60 * 1000;
    private String transformationName = getDefaultTransformationName();

    abstract MimeTypeService getMimeTypeService();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getTransformationName() {
        return transformationName;
    }

    /** @return Returns true if VIPS is installed and can be invoked here **/
    protected boolean checkVips() {
        return checkVips(false);
    }

    protected boolean checkVips(boolean force) {
        if(force || lastCheckTime + checkTimeout < System.currentTimeMillis()) {
            lastCheckTime = System.currentTimeMillis();
            ProcessRunner runner = new ProcessRunner();
            List<String> commands = new ArrayList<>(Arrays.asList(VIPS, VERSION_PARAMETER));
            try {
                ProcessContext processContext = runner.execute(commands);
                vipsInstalled = processContext.getExitCode() == 0;
            } catch(ExternalProcessException e) {
                vipsInstalled = false;
            }
        }
        return vipsInstalled;
    }


    protected void configure(boolean enabled, String transformationName) {
        this.enabled = enabled;
        this.transformationName = transformationName == null || transformationName.isEmpty() ?
            getDefaultTransformationName() :
            transformationName;
        if(enabled) {
            if(transformationName.isEmpty()) {
                throw new IllegalArgumentException(TRANSFORMATION_NAME_CANNOT_BE_EMPTY);
            }
            checkVips(true);
        }
    }

    /**
     * Transforms an image using the given Operation Name and Parameters
     *
     * @param imageContext Context of the Image to be transformed which cannot be null
     * @param operationName VIPS Operation Name which cannot be empty
     * @param parameters Optional Parameters for the VIPS Operation
     * @throws TransformationException If image context is null, operation name is empty, files cannot be created
     *         or the VIPS process executions fails
     */
    protected void transform0(ImageContext imageContext, String operationName, String...parameters)
        throws TransformationException
    {
        if(!enabled) {
            log.debug("Image Transformation: '{}' is not enabled and so it is ignored", transformationName);
        } else {
            if (imageContext == null) {
                throw new TransformationException(IMAGE_CONTEXT_MUST_BE_DEFINED_FOR_TRANSFORMATION);
            }
            if (isEmpty(operationName)) {
                throw new TransformationException(VIPS_OPERATION_NAME_CANNOT_BE_EMPTY);
            }
            if (checkVips()) {
                String sourceExtension = getMimeTypeService().getExtension(imageContext.getSourceMimeType());
                if (sourceExtension == null) {
                    sourceExtension = imageContext.getSourceMimeType();
                }
                String targetExtension = getMimeTypeService().getExtension(imageContext.getTargetMimeType());
                if (targetExtension == null) {
                    targetExtension = imageContext.getTargetMimeType();
                }
                String name = "transformation.in." + System.currentTimeMillis() + "." + sourceExtension;
                Path temporaryFolder = createTempFolder();
                if (temporaryFolder != null) {
                    File input = writeToFile(temporaryFolder, name, imageContext.getImageStream());
                    if (input == null) {
                        throw new TransformationException(COULD_NOT_CREATE_INPUT_FILE + name);
                    }
                    String outputFileName = "transformation.out." + System.currentTimeMillis() + "." + targetExtension;
                    File output = createTempFile(temporaryFolder, outputFileName);
                    if (output == null) {
                        throw new TransformationException(COULD_NOT_CREATE_OUTPUT_FILE + outputFileName);
                    }
                    ProcessRunner runner = new ProcessRunner();
                    List<String> commands = new ArrayList<>(Arrays.asList(VIPS, operationName));
                    boolean inputUsed = false, outputUsed = false;
                    for (String parameter : parameters) {
                        if (IN_TOKEN.equals(parameter)) {
                            commands.add(input.getAbsolutePath());
                            inputUsed = true;
                        } else if (OUT_TOKEN.equals(parameter)) {
                            commands.add(output.getAbsolutePath());
                            outputUsed = true;
                        } else {
                            commands.add(parameter);
                        }
                    }
                    if (!inputUsed) {
                        throw new IllegalArgumentException(INPUT_WAS_NOT_USED_IN);
                    }
                    InputStream inputStream = null;
                    try {
                        ProcessContext processContext = runner.execute(commands);
                        if (processContext.getExitCode() > 0) {
                            throw new TransformationException(String.format(FAILED_TO_EXECUTE_VIPS_OPERATION_WITH_CODE, operationName, processContext.getExitCode()));
                        }
                        if (outputUsed) {
                            inputStream = openFileInput(temporaryFolder, output.getName());
                        } else {
                            inputStream = openFileInput(temporaryFolder, input.getName());
                        }
                        imageContext.resetImageStream(inputStream);
                    } catch (ExternalProcessException e) {
                        if(!log.isDebugEnabled()) {
                            log.warn("Failed to execute VIPS command: " + operationName);
                        } else {
                            log.debug("Failed to execute VIPS command: " + operationName, e);
                        }
                        throw new TransformationException(FAILED_TO_EXECUTE_VIPS_OPERATION + operationName, e);
                    }
                } else {
                    throw new TransformationException(COULD_NOT_CREATE_TEMPORARY_FOLDER + name);
                }
            } else {
                log.warn("VIPS not installed -> ignore transformation: '{}'", transformationName);
            }
        }
    }

    /** @return Created Temporary Folder. If it fails it returns null **/
    private Path createTempFolder() {
        Path dir = null;
        try {
            dir = Files.createTempDirectory(null);
            dir.toFile().deleteOnExit();
        } catch(IOException e) {
            log.error("Failed to create temporary folder", e);
        }
        return dir;
    }

    /** @return Created Temporary File based on the temporary folder and file name. If it fails it returns null **/
    private File createTempFile(Path tempFolder, String fileName) {
        File file = null;
        try {
            file = tempFolder.resolve(fileName).toFile();
            file.deleteOnExit();
        } catch(InvalidPathException e) {
            log.error("Failed to create temporary file", e);
        }
        return file;
    }

    /**
     * Writes the given input stream into the given file
     *
     * @param directory Folder where the file is placed into
     * @param fileName File Name of the file to be created including extension
     * @param inputStream Data that is written to the file. This Input Stream is closed at the end
     * @return
     */
    private File writeToFile(Path directory, String fileName, InputStream inputStream) {
        File output = null;
        FileOutputStream fos = null;
        try {
            output = directory.resolve(fileName).toFile();
            output.deleteOnExit();
            fos = new FileOutputStream(output);
            IOUtils.copy(inputStream, fos);
        } catch(InvalidPathException e) {
            log.error("Failed to create Output File", e);
        } catch(FileNotFoundException e) {
            log.error("Failed to create File Output Stream", e);
            if(output != null && output.exists()) {
                output.delete();
            }
            output = null;
        } catch(IOException e) {
            log.error("Failed to write to file", e);
            if(output != null && output.exists()) {
                output.delete();
            }
            output = null;
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(inputStream);
        }
        return output;
    }

    /** @return Created File Input Stream or null if it failed **/
    private InputStream openFileInput(Path directory, String fileName) {
        File output = null;
        FileInputStream fis = null;
        try {
            output = directory.resolve(fileName).toFile();
            if(output.isFile() && output.exists()) {
                output.deleteOnExit();
                fis = new FileInputStream(output);
            } else {
                log.error("File: '{}' does not exist", output.getPath());
            }
        } catch(FileNotFoundException e) {
            log.error("Failed to write to file", e);
        }
        return fis;
    }

}
