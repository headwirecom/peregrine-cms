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

import java.io.Reader;

/**
 * Context of an External Process Execution
 * Created by Andreas Schaefer on 4/5/17.
 */
public interface ProcessContext {

    public static final int NO_EXIT_CODE = -99999;

    /** @return Exit code of the process execution **/
    public int getExitCode();

    /** @return Output of the process **/
    public String getOutput();

    /** @return Output of the process as Reader **/
    public Reader getOutputReader();

    /** @return Error Message if the process errored out **/
    public String getError();

    /** @return Error Message as reader **/
    public Reader getErrorReader();

    /** Tear the process down (free all resources) **/
    public void tearDown();
}
