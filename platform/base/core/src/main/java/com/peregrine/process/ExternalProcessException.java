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

import java.util.List;

/**
 * Exception from executing an External Process
 *
 * Created by Andreas Schaefer on 4/4/17.
 */
public class ExternalProcessException
    extends Exception
{
    /** List of all commands provided to the process **/
    private List<String> command = null;
    /** Process Context **/
    private ProcessContext processContext;

    public ExternalProcessException(String message) {
        super(message);
    }

    public ExternalProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return List of commands provided to the process 
     */
    public List<String> getCommand() {
        return command;
    }

    /** 
     * Sets the list of commands 
     * @param command List of commands
     * @return external process exception
     */
    public ExternalProcessException setCommand(List<String> command) {
        this.command = command;
        return this;
    }

    /** @return External Process Context **/
    public ProcessContext getProcessContext() {
        return processContext;
    }

    /** Sets the External Process Context **/
    public ExternalProcessException setProcessContext(ProcessContext processContext) {
        this.processContext = processContext;
        return this;
    }
}
