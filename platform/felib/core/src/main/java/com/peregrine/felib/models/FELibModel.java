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
import org.apache.sling.api.resource.ValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Created by rr on 4/18/2017.
 */
public class FELibModel {

    private static final String DEPENDENCIES = "dependencies";
    private static final String BASE_PREFIX = "base=";

    private final String fileName;
    private final Resource resource;
    private final ResourceResolver resolver;
    private final String path;

    protected FELibModel(final Resource resource, final String fileName) {
        this.resource = resource;
        this.fileName = fileName;
        resolver = resource.getResourceResolver();
        path = resource.getPath();
    }

    public List<JCRFile> getFiles() {
        return getFiles(resource);
    }

    private List<JCRFile> getFiles(final Resource node) {
        final List<JCRFile> result = getDependencies(node);
        final Resource definitionNode = node.getChild(fileName);
        if (nonNull(definitionNode)) {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(definitionNode.adaptTo(InputStream.class)))) {
                String basePath = node.getPath();
                String line = br.readLine();
                while (nonNull(line)) {
                    final String text = line.trim();
                    basePath = processLineAndGetBasePath(node, text, basePath, result);
                    line = br.readLine();
                }
            } catch (final IOException e) {
                throw new RuntimeException("failed to read file", e);
            }
        }

        return result;
    }

    private List<JCRFile> getDependencies(Resource node) {
        final List<JCRFile> result = new LinkedList<>();
        final ValueMap properties = node.getValueMap();
        if (properties.containsKey(DEPENDENCIES)) {
            final String[] dependents = properties.get(DEPENDENCIES, String[].class);
            for (final String dependent : dependents) {
                final Resource resourceDependent = resolver.getResource(dependent);
                result.addAll(getFiles(resourceDependent));
            }
        }

        return result;
    }

    private String processLineAndGetBasePath(final Resource node, final String line, final String basePath, final List<JCRFile> target) throws IOException {
        if (isComment(line) || isBlank(line)) {
            return basePath;
        }

        if (line.startsWith(BASE_PREFIX)) {
            return getAbsolutePath(node, line.substring(BASE_PREFIX.length()));
        }

        final String path = getAbsolutePath(basePath, line);
        final Optional<InputStream> is = Optional.of(path)
                .map(resolver::getResource)
                .map(r -> r.adaptTo(InputStream.class));
        if (is.isPresent()) {
            final String content = IOUtils.toString(is.get(), UTF_8);
            target.add(new JCRFile(path, content));
        }

        return basePath;
    }

    private boolean isComment(final String text) {
        return text.startsWith("#");
    }

    private String getAbsolutePath(final String base, final String relPath) {
        if (relPath.startsWith(SLASH)) {
            return relPath;
        } else {
            return base + SLASH + relPath;
        }
    }

    private String getAbsolutePath(final Resource node, final String relPath) {
        return getAbsolutePath(node.getPath(), relPath);
    }

}
