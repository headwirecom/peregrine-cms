package com.peregrine.admin.servlets;

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

import org.apache.sling.api.SlingHttpServletRequest;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rr on 5/2/2017.
 */
public class ServletHelper {

    public static Map<String,String> convertSuffixToParams(SlingHttpServletRequest request) {

        String suffix = request.getRequestPathInfo().getSuffix();
        HashMap<String, String> ret = new HashMap<String, String>();

        if(suffix != null && suffix.length() > 1) {
            suffix = suffix.substring(1);
            String[] params = suffix.split("//");
            // We loop in 2 steps but the check makes sure that there are two entries left
            for(int i = 0; i < params.length - 1; i+=2) {
                ret.put(params[i], params[i+1]);
            }
        }

        return ret;

    }

    public static String componentPathToName(String path) {
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < segments.length; i++) {
            if(i != 2) {
                sb.append("-");
            }
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    public static String componentNameToPath(String name) {
        String[] segments = name.split("-");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < segments.length; i++) {
            if(i != 0) {
                sb.append("/");
            }
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    public static void echo(InputStream input, PrintWriter writer) throws IOException {
        char[] buffer = new char[1024];
        InputStreamReader reader = new InputStreamReader(input);
        int chars = reader.read(buffer);
        while(chars > 0) {
            writer.write(buffer, 0, chars);
            chars = reader.read(buffer);
        }
        reader.close();
    }

    public static StringWriter asString(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        char[] buffer = new char[1024];
        InputStreamReader reader = new InputStreamReader(input);
        int chars = reader.read(buffer);
        while(chars > 0) {
            writer.write(buffer, 0, chars);
            chars = reader.read(buffer);
        }
        reader.close();
        return writer;
    }
}
