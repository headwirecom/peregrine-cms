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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.reference.ReferenceLister;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.List;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_REF;
import static com.peregrine.admin.servlets.NodesServlet.CHILDREN;
import static com.peregrine.admin.servlets.ReferenceServletUtils.*;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.isNull;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet provides a list of resources that are referenced by the given
 * resource (to which resources does the given resources points to)
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Reference Lister Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_REF,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
public class ReferenceListerServlet extends AbstractBaseServlet {

    public static final String REFERENCES = "references";
    public static final String IS_ASSETS_FOLDER = "is_assets_folder";

    @Reference
    private ReferenceLister referenceLister;

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final String sourcePath = request.getParameter(PATH);
        final Resource source = request.getResourceResolver().getResource(sourcePath);
        if (isNull(source)) {
            return badRequest(sourcePath);
        }

        final JsonResponse answer = new JsonResponse();
        addBasicSourceProps(source, answer);
        addReplicationProps(source, answer);
        try {
            boolean isAssetsFolder = resourceManagement.isAssetsFolder(source);
            if (isAssetsFolder) {
                answer.writeAttribute(IS_ASSETS_FOLDER, true);
                answer.writeArray(CHILDREN);
                for (Resource child : source.getChildren()) {
                    if (!resourceManagement.isAssetsFolder(child)) {
                        answer.writeObject();
                        addBasicProps(child, answer);
                        addReplicationProps(child, answer);
                        answer.writeClose();
                    }
                }
                answer.writeClose();
            }
        } catch (RepositoryException e) {
            logger.error("Failed to check resource is folder for {}", source.getPath(), e);
        }
        final List<Resource> references = referenceLister.getReferenceList(true, source, true, getChecker(request));
        answer.writeArray(REFERENCES);
        for (final Resource reference : references) {
            answer.writeObject();
            addBasicProps(reference, answer);
            addReplicationProps(reference, answer);
            answer.writeClose();
        }

        answer.writeClose();
        return answer;
    }

}
