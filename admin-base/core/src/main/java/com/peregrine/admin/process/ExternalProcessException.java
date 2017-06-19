package com.peregrine.admin.process;

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
 * Created by schaefa on 4/4/17.
 */
public class ExternalProcessException
    extends Exception
{
    private List<String> command = null;
    private ProcessContext processContext;

    public ExternalProcessException(String message) {
        super(message);
    }

    public ExternalProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public List<String> getCommand() {
        return command;
    }

    public ExternalProcessException setCommand(List<String> command) {
        this.command = command;
        return this;
    }

    public ProcessContext getProcessContext() {
        return processContext;
    }

    public ExternalProcessException setProcessContext(ProcessContext processContext) {
        this.processContext = processContext;
        return this;
    }
}
