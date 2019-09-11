package com.peregrine.transform;

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

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Context of an Image Transformation Operation
 *
 * Created by Andreas Schaefer on 5/19/17.
 */
public class OperationContext {

    /** Name of the Image Transformation Operation **/
    private String operationName;
    /** Parameters Map of the Operation **/
    private Map<String, String> parameters = new HashMap<>();

    /**
     * Creates an Image Transformation Operation Context
     * @param operationName Name of the Operation which cannot be empty
     * @param parameters Optional Map of Parameters. Is ignored if null or empty
     */
    public OperationContext(String operationName, Map<String, String> parameters) {
        if(isEmpty(operationName)) {
            throw new IllegalArgumentException("Operation Name cannot be null or empty");
        }
        this.operationName = operationName;
        if(parameters != null && !parameters.isEmpty()) {
            this.parameters.putAll(parameters);
        }
    }

    /** @return Operation Name which is never empty **/
    public String getOperationName() {
        return operationName;
    }

    /** @return Optional Parameter Map. Which is never null but can be empty **/
    public Map<String, String> getParameters() {
        return parameters;
    }

    /** 
     * @param name Name of parameter
     * @return Single Parameter denoted by the given name. If not found is null
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * @param name Name of parameter
     * @param defaultValue Fallback value
     * @return Single Parameter denoted by the given name. If not found or empty then given default value is returned instead 
     */
    public String getParameter(String name, String defaultValue) {
        String answer = defaultValue;
        if(parameters.containsKey(name)) {
            String temp = parameters.get(name);
            if(isNotEmpty(temp)) {
                answer = temp;
            }
        }
        return answer;
    }
}
