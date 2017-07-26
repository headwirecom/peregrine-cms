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

/**
 * Created by schaefa on 5/19/17.
 */
public class OperationContext {

    private String operationName;
    private Map<String, String> parameters;

    public OperationContext(String operationName, Map<String, String> parameters) {
        if(operationName == null || operationName.isEmpty()) {
            throw new IllegalArgumentException("Operation Name cannot be null or empty");
        }
        this.operationName = operationName;
        this.parameters = new HashMap<>();
        if(parameters != null && !parameters.isEmpty()) {
            this.parameters.putAll(parameters);
        }
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getParameter(String name, String defaultValue) {
        String answer = defaultValue;
        if(parameters.containsKey(name)) {
            String temp = parameters.get(name);
            if(temp != null) {
                answer = temp;
            }
        }
        return answer;
    }
}
