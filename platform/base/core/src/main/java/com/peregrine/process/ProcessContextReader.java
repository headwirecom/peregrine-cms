package com.peregrine.process;

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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Created by Andreas Schaefer on 4/5/17.
 */
public class ProcessContextReader
    implements ProcessContext
{
    private final Logger log = LoggerFactory.getLogger(ProcessContextReader.class);

    private int exitCode;
    private File outputFile;
    private File errorFile;

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public ProcessContextReader setExitCode(int exitCode) {
        this.exitCode = exitCode;
        return this;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public ProcessContextReader setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public File getErrorFile() {
        return errorFile;
    }

    public ProcessContextReader setErrorFile(File errorFile) {
        this.errorFile = errorFile;
        return this;
    }

    /**
     * Deletes the given files and its parent if there is a file
     */
    public void tearDown() {
        File parent = null;
        if(outputFile != null) {
            parent = outputFile.getParentFile();
        } else if(errorFile != null){
            parent = errorFile.getParentFile();
        }
        if(errorFile != null) {
            errorFile.delete();
        }
        if(outputFile != null) {
            outputFile.delete();
        }
        if(parent != null) {
            parent.delete();
        }
    }

    @Override
    public String getOutput() {
        return getFileContent(outputFile);
    }

    @Override
    public Reader getOutputReader() {
        try {
            return new FileReader(outputFile);
        } catch(FileNotFoundException e) {
            // Should not happen
            return null;
        }
    }

    @Override
    public String getError() {
        return getFileContent(errorFile);
    }

    @Override
    public Reader getErrorReader() {
        try {
            return new FileReader(errorFile);
        } catch(FileNotFoundException e) {
            // Should not happen
            return null;
        }
    }

    private String getFileContent(File file) {
        String answer = "";
        if(file != null) {
            StringWriter writer = null;
            FileReader reader = null;
            try {
                writer = new StringWriter();
                reader = new FileReader(file);
                IOUtils.copy(reader, writer);
            } catch(FileNotFoundException e) {
                log.error("Failed to find file: '" + file + "' (should not happen)", e);
            } catch(IOException e) {
                log.error("Failed to read file: '" + file + "' (should not happen)", e);
            } finally {
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(writer);
            }
            answer = writer.toString();
        } else {
            log.warn("File it not specified to read from");
        }
        return answer;
    }
}
