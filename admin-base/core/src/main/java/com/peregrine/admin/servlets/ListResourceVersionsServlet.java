
/*-
 * #%L
 * admin base - Core
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
 * Contributed by Cris Rockwell, University of Michigan
 */

package com.peregrine.admin.servlets;


import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Arrays;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_VERSIONS;
import static com.peregrine.admin.servlets.ListSiteRecyclablesServlet.DELETED_DATE_FORMAT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;


/**
 * List versions for a resource at the path
 * expects the resource path (e.g. /content/example/pages/index) as a Sling suffix
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Resource Versions",
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_VERSIONS
    }
)
@SuppressWarnings("serial")
public class ListResourceVersionsServlet extends AbstractBaseServlet {

    public static final String FAILED_TO_LIST_VERSIONS = "Unable to list versions";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String HAS_VERSIONS = "has_versions";

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final String resourcePath = request.getSuffix();
        VersionIterator vi = null;
        Resource resource = null;
        Resource resourceContent = null;
        JsonResponse answer = new JsonResponse();
        try {
            if (resourcePath != null && !resourcePath.isEmpty()) {
                resource = request.getResourceResolver().getResource(resourcePath);
                if (resource == null) {
                    return new ErrorResponse().setHttpErrorCode(SC_NOT_FOUND).setErrorMessage(RESOURCE_NOT_FOUND);
                }
                resourceContent = resource.getChild(JCR_CONTENT);
                if (resourceContent == null) {
                    resourceContent = resource;
                }
                vi = resourceManagement.getVersionIterator(request.getResourceResolver(), resourceContent);
            } else {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(FAILED_TO_LIST_VERSIONS);
            }

            if (vi == null ) {
                // If the version iterator is null, then the resource has no versions.
                answer.writeAttribute(HAS_VERSIONS, false);
                answer.writeClose();
                return answer;
            }
            // Root version contains no versioning data
            Version root = vi.nextVersion();
            if (vi.hasNext()) {
                answer.writeAttribute(HAS_VERSIONS, true);
            } else {
                // No versions other than root
                answer.writeAttribute(HAS_VERSIONS, false);
                answer.writeClose();
                return answer;
            }
            // write an array of version
            answer.writeArray("versions");
            Version base = resourceManagement.getBaseVersion(request.getResourceResolver(), resourceContent.getPath());
            do {
                Version v = vi.nextVersion();
                answer.writeObject();
                answer.writeAttribute("name", v.getName());
                answer.writeAttribute("created", DELETED_DATE_FORMAT.format(v.getCreated().getTime()));
                answer.writeAttribute("path", v.getPath());
                String[] labels = v.getContainingHistory().getVersionLabels(v);
                if (labels.length > 0) {
                    answer.writeArray("labels");
                    for(int i =0; i<labels.length; i++){
                        answer.writeString(labels[i]);
                    }
                    answer.writeClose();
                }
                if (base.getName().equals(v.getName())) {
                    answer.writeAttribute("base", true);
                } else {
                    answer.writeAttribute("base", false);
                }
                answer.writeClose();
            } while (vi.hasNext());
            answer.writeClose();
            return answer;
        } catch (Exception e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_LIST_VERSIONS)
                .setException(e);
        }
    }
}

