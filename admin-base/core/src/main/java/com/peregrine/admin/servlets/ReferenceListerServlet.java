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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_REF;
import static com.peregrine.admin.servlets.ReferenceServletUtils.addBasicProps;
import static com.peregrine.admin.servlets.ReferenceServletUtils.addBasicSourceProps;
import static com.peregrine.admin.servlets.ReferenceServletUtils.addReplicationProps;
import static com.peregrine.admin.servlets.ReferenceServletUtils.badRequest;
import static com.peregrine.admin.servlets.ReferenceServletUtils.getChecker;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static java.util.Objects.isNull;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.reference.ReferenceLister;
import java.io.IOException;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

    @Reference
    private ReferenceLister referenceLister;

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
