package com.peregrine.commons.servlets;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.DASH;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;

/**
 * Helper Methods for Servlets
 *
 * Created by rr on 5/2/2017.
 */
public class ServletHelper {

    public static final int BUFFER_LENGTH = 1024;

    /**
     * Provides the Request Parameters. The parameters are
     * either retrieved from the Suffix or from the Servlet Parameters
     * @param request Request containing the parameters
     * @return A map of parameters
     */
    public static Map<String,String> obtainParameters(SlingHttpServletRequest request) {
        HashMap<String, String> answer = new HashMap<String, String>();

        String suffix = request.getRequestPathInfo().getSuffix();
        if(suffix != null && !suffix.isEmpty()) {
            answer.put(PATH, suffix);
        }
        Enumeration<String> e = request.getParameterNames();
        while(e.hasMoreElements()) {
            String name = e.nextElement();
            String value = request.getParameter(name);
            answer.put(name, value);
        }

        return answer;

    }

    /**
     * Splits the given path into segments (by /) and
     * then adds each segments (except the first)
     * separated by a dash.
     *
     * For example '/apps/it/components/test' would yield: 'it-components-test'
     * @param path Path to be handled
     * @return Dash separated Component Name
     */
    public static String componentPathToName(String path) {
        String[] segments = path.split(SLASH);
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < segments.length; i++) {
            if(i != 2) {
                sb.append(DASH);
            }
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    /**
     * Creates a Component Path from the given name. If will
     * be missing the leading '/apps/'.
     *
     * For example 'it-components-test' would yield: 'it/components/test'
     * @param name Name to be converted
     * @return Component path relative to /apps
     */
    public static String componentNameToPath(String name) {
        String[] segments = name.split(DASH);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < segments.length; i++) {
            if(i != 0) {
                sb.append(SLASH);
            }
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    /**
     * Echoes the given Input Stream into a Print Writer
     * @param input Input Stream which is read out
     * @param writer Print Writer to which the content of the input stream is sent to
     * @throws IOException If the handling failed
     */
    public static void echo(InputStream input, PrintWriter writer) throws IOException {
        char[] buffer = new char[BUFFER_LENGTH];
        InputStreamReader reader = new InputStreamReader(input);
        int chars = reader.read(buffer);
        while(chars > 0) {
            writer.write(buffer, 0, chars);
            chars = reader.read(buffer);
        }
        reader.close();
    }

    /**
     * Returns the content of the Input Stream as String Writer
     * @param input Input Stream which is read out
     * @return String Writer instance that contains the Input Stream's content
     * @throws IOException If the handling failed
     */
    public static StringWriter asString(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        char[] buffer = new char[BUFFER_LENGTH];
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
