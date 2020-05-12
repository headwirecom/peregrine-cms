package com.peregrine.admin.servlets;

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
 */

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.DeletionResponse;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DELETE_PAGE;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_RECYCLABLES;
import static com.peregrine.admin.util.AdminConstants.DATA;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * List all the recoverable items for a given site
 *
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Site Recyclables",
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_RECYCLABLES
    }
)
@SuppressWarnings("serial")
public class ListSiteRecyclablesServlet extends AbstractBaseServlet {

    public static final String FAILED_TO_LIST_RECYCLABLES = "Failed get recyclable list :-/ ";

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final String path = request.getParameter(PATH);
        final Session session = request.getResourceResolver().adaptTo(Session.class);
        JsonResponse answer = new JsonResponse();
        final String sitePath = request.getSuffix();
        //+sitePath+"%'"
        //SELECT * from "nt:versionHistory" where default  like "/content/example/%"
        //final String queryStr = "SELECT * FROM [nt:frozenNode] as n WHERE ISDESCENDANTNODE ([/jcr:system/jcr:versionStorage]) AND n.[jcr:path] LIKE '/jcr:system/jcr:versionStorage/%'";
        //  AND [jcr:primaryType] = 'per:Page'
        final String queryStr = "SELECT * from [nt:versionHistory] where default like '"+sitePath+"%'";
        try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(queryStr, Query.JCR_SQL2);
            QueryResult res = q.execute();
            NodeIterator nodes = res.getNodes();
            answer.writeArray(DATA);
            while(nodes.hasNext()) {
                Node node = nodes.nextNode();

                answer.writeObject();
                answer.writeAttribute(NAME, node.getName());
                answer.writeAttribute(PATH, node.getPath());
                answer.writeClose();
            }
            answer.writeClose();
            return answer;
        } catch (RepositoryException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(FAILED_TO_LIST_RECYCLABLES).setException(e);
        }
//        try {
            //resourceManagement
//            return new JsonResponse()
//                .writeAttribute(TYPE, PAGE)
//                .writeAttribute(STATUS, DELETED)
//                .writeAttribute(NAME, response.getName())
//                .writeAttribute(NODE_TYPE, response.getType())
//                .writeAttribute(PARENT_PATH, response.getParentPath());
//        } catch (ManagementException e) {
//            return new ErrorResponse()
//                .setHttpErrorCode(SC_BAD_REQUEST)
//                .setErrorMessage(FAILED_TO_DELETE_NODE + path)
//                .setRequestPath(path).setException(e);
//        }
    }
}

