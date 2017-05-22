package com.peregrine.admin.transform;

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
