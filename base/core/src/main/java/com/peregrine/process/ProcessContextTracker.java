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

import java.io.Reader;
import java.io.StringReader;

/**
 * Created by schaefa on 4/5/17.
 */
public class ProcessContextTracker
    implements ProcessContext
{
    private final Logger log = LoggerFactory.getLogger(ProcessContextTracker.class);

    private int exitCode = 0;
    private String output = "";
    private String error = "";

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public ProcessContextTracker setExitCode(int exitCode) {
        this.exitCode = exitCode;
        return this;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public Reader getOutputReader() {
        return new StringReader(output);
    }

    public ProcessContextTracker appendOutput(String text) {
        output += text;
        return this;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public Reader getErrorReader() {
        return new StringReader(error);
    }

    public ProcessContextTracker appendError(String text) {
        if(text != null && !text.isEmpty()) {
            exitCode = 1;
        }
        error += text;
        return this;
    }

    @Override
    public void tearDown() {}
}
