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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_REF_BY;
import static com.peregrine.admin.servlets.NodesServlet.ACTIVATED;
import static com.peregrine.admin.servlets.NodesServlet.DATE_FORMATTER;
import static com.peregrine.admin.servlets.ReferenceListerServlet.IS_STALE;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.adaption.PerReplicable;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.replication.ReferenceLister;
import java.io.IOException;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides a list of references that have a reference to the
 * given resource (which resources point to the given resources)
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Referenced By Lister Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_REF_BY,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
public class ReferencedByListerServlet extends AbstractBaseServlet {

    public static final String REFERENCED_BY = "referencedBy";
    public static final String PROPERTY_NAME = "propertyName";
    public static final String PROPERTY_PATH = "propertyPath";
    public static final String GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE = "Given Path does not yield a resource";

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String sourcePath = request.getParameter (PATH);
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<com.peregrine.replication.Reference> references = referenceLister.getReferencedByList(source);
            JsonResponse answer = new JsonResponse();
            answer.writeAttribute(SOURCE_NAME, source.getName());
            answer.writeAttribute(SOURCE_PATH, source.getPath());
            PerReplicable sourceRepl = source.adaptTo(PerReplicable.class);
            answer.writeAttribute(ACTIVATED, sourceRepl.isReplicated());
            if (sourceRepl.getReplicated() != null && sourceRepl.getReplicated() != null) {
                answer.writeAttribute(PER_REPLICATED, DATE_FORMATTER.format(sourceRepl.getReplicated().getTime().getTime()));
            }
            if (sourceRepl.getLastModified()!=null){
                answer.writeAttribute(JCR_LAST_MODIFIED, DATE_FORMATTER.format(sourceRepl.getLastModified().getTime()));
            }
            if (sourceRepl.getLastModified()!=null && sourceRepl.getReplicated()!= null) {
                answer.writeAttribute(IS_STALE, sourceRepl.isStale());
            }
            answer.writeArray(REFERENCED_BY);
            for(com.peregrine.replication.Reference child : references) {
                answer.writeObject();
                answer.writeAttribute(NAME, child.getResource().getName());
                answer.writeAttribute(PATH, child.getResource().getPath());
                answer.writeAttribute(PROPERTY_NAME, child.getPropertyName());
                answer.writeAttribute(PROPERTY_PATH, child.getPropertyResource().getPath());
                PerReplicable childRepl = child.getResource().adaptTo(PerReplicable.class);
                answer.writeAttribute(ACTIVATED, childRepl.isReplicated());
                if (childRepl.getLastModified()!=null){
                    answer.writeAttribute(JCR_LAST_MODIFIED, DATE_FORMATTER.format(childRepl.getLastModified().getTime()));
                }
                if (childRepl.getReplicated()!=null){
                    answer.writeAttribute(PER_REPLICATED, DATE_FORMATTER.format(childRepl.getReplicated().getTime()));
                }
                if (childRepl.getLastModified()!=null && childRepl.getReplicated()!= null) {
                    answer.writeAttribute(IS_STALE, childRepl.isStale());
                }
                answer.writeClose();
            }
            return answer;
        } else {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE)
                .setRequestPath(sourcePath);
        }
    }
}
