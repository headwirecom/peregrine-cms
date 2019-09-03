package com.peregrine.felib.models;

/*-
 * #%L
 * peregrine-felib - Core
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

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by rr on 4/18/2017.
 */
public class FELibModel {

    public static final String SLASH = "/";

    @Self
    private Resource node;

    public ArrayList<JCRFile> getFiles(String fileName) {
        return getFiles(node, fileName);
    }

    public ArrayList<JCRFile> getFiles(Resource node, String fileName) {
        ArrayList<JCRFile> ret = new ArrayList<JCRFile>();
        ResourceResolver rr = node.getResourceResolver();
        if(node.getValueMap().containsKey("dependencies")) {
            String[] dependents = node.getValueMap().get("dependencies", String[].class);
            for (String dependent: dependents) {
                Resource resourceDependent = rr.getResource(dependent);
                ret.addAll(getFiles(resourceDependent, fileName));
            }
        }

        Resource definitionNode = node.getChild(fileName);
        if(definitionNode != null) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(definitionNode.adaptTo(InputStream.class)));

                String basePath = node.getPath();
                String line = br.readLine();
                while (line != null) {
                    if (!(line.startsWith("#") || line.trim().length() == 0)) {
                        if(line.startsWith("base=")) {
                            String path = line.substring(5);
                            basePath = node.getPath()+SLASH+path;
                            if(path.startsWith(SLASH)) {
                                basePath = path;
                            }
                        } else {
                            String filePath = basePath+SLASH+line;
                            if(line.startsWith(SLASH)) {
                                filePath = line;
                            }
                            Resource file = rr.getResource(filePath);
                            if(file != null) {
                                InputStream is = file.adaptTo(InputStream.class);
                                String data = IOUtils.toString(is, "UTF-8");
                                ret.add(new JCRFile(basePath + SLASH + line, data));
                            }
                        }
                    }
                    line = br.readLine();
                }

            } catch (IOException ioe) {
                throw new RuntimeException("failed to read file", ioe);
            } finally {
                try {
                    if (br != null) br.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }
        return ret;
    }

}
