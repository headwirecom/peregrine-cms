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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static com.peregrine.process.ProcessContext.NO_EXIT_CODE;

/**
 * Executes an External Process defined by a list of commands (command followed by 0+ arguments)
 *
 * Created by Andreas Schaefer on 4/6/17.
 */
public class ProcessRunner {

    public static final String OS_NAME = "os.name";
    public static final String GENERIC = "generic";
    public static final String FAILED_TO_EXECUTE_PROCESS = "Failed to execute process";
    
    private final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

    /** Working Directory in my to place the output and error files. Current user folder is default. **/
    private File workingDirectory = new File(".");

    public ProcessRunner() {}

    public ProcessRunner(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /** @return True if this is executed on Windows OS **/
    public boolean isWindows() {
        String OS = System.getProperty(OS_NAME, GENERIC).toLowerCase(Locale.ENGLISH);
        return OS.startsWith("win");
    }

    /**
     * Executes an External Process and waits for it to conclude
     * @param command List of strings where the first is the command followed by none or more arguments
     * @return Process Context of the Execution
     * @throws ExternalProcessException If the Execution failed because the output / error files could not be created
     *                                  or the External Process threw an IO Exception. This is not thrown when
     *                                  the External Process exists with an error
     */
    public ProcessContext execute(List<String> command)
        throws ExternalProcessException
    {
        log.trace("Execute Command: '{}'", command);
        Path dir = null;
        try {
            dir = Files.createTempDirectory(null);
        } catch(IOException e) {
            throw new ExternalProcessException("Failed to create temporary directory to catch output", e);
        }

        final File output = dir.resolve("output.txt").toFile();
        output.deleteOnExit();
        final File error = dir.resolve("error.txt").toFile();
        error.deleteOnExit();
        log.trace("Output File: '{}', Error File: '{}'", output.getPath(), error.getPath());
        ProcessContextReader answer = new ProcessContextReader().setOutputFile(output).setErrorFile(error);

        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDirectory);
        pb.redirectOutput(Redirect.to(output));
        pb.redirectError(Redirect.to(error));

        int exitCode = NO_EXIT_CODE;
        try {
            final Process p = pb.start();
            try {
                exitCode = p.waitFor();
                log.trace("Exit Code: '{}'", exitCode);
            } catch(final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch(IOException e) {
            log.trace("IO Exception", e);
            throw new ExternalProcessException(FAILED_TO_EXECUTE_PROCESS, e).setCommand(command);
        }

        return answer.setExitCode(exitCode);
    }
}
