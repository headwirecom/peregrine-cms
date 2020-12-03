
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

import com.peregrine.admin.models.Recyclable;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.xss.XSSAPI;
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
import java.text.SimpleDateFormat;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_RECYCLABLES;
import static com.peregrine.admin.util.AdminConstants.*;
import static com.peregrine.admin.util.AdminConstants.DATA;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;


/**
 * List all the recoverable items for a given site
 * expects the site root path (e.g. /content/example) aa a Sling suffix
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
    public static final String ACL_FOR_RECYCLABLES_INSUFF = "Insufficient permissions to view or restore from recycle bin";
    public static final String DELETED_BY = "deleted_by";
    public static final String DATE_DELETED = "date_deleted";
    public static final String READ_PERMISSIONS = "READ_NODE,READ_PROPERTY";
    public static final String VERSION_PERMISSIONS = "VERSION_MANAGEMENT";
    public static final SimpleDateFormat DELETED_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy h:mm a");
    private static final long ROWS_PER_PAGE = 100;

    @Reference
    AdminResourceHandler resourceManagement;

    @Reference
    private XSSAPI xssapi;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final Session session = request.getResourceResolver().adaptTo(Session.class);
        JsonResponse answer = new JsonResponse();
        final String sitePath = request.getSuffix();

        if (sitePath == null || sitePath.isEmpty() || sitePath.length() > 300) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(FAILED_TO_LIST_RECYCLABLES);
        }

        if (!resourceManagement.hasPermission(request.getResourceResolver(), READ_PERMISSIONS, RECYCLE_BIN_PATH+sitePath) ||
                !resourceManagement.hasPermission(request.getResourceResolver(), VERSION_PERMISSIONS, sitePath)) {
            return new ErrorResponse().setHttpErrorCode(SC_FORBIDDEN).setErrorMessage(ACL_FOR_RECYCLABLES_INSUFF);
        }

        // Set up pagination
        String pageParam = request.getParameter(PAGE, "0");
        int page = 0;
        try {
            page = Integer.parseInt(pageParam);
        } catch(NumberFormatException e) {
            logger.warn("Given Page: {} could not be converted to an integer -> ignored", pageParam, e);
        }

        final String queryStr =  "SELECT * from [nt:unstructured] as r " +
            "WHERE [sling:resourceType]='admin/components/recyclable' "+
            "AND ISDESCENDANTNODE(r, '" + RECYCLE_BIN_PATH + sitePath + "') ORDER BY [jcr:created] DESC";

        try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(queryStr, Query.JCR_SQL2);
            q.setLimit(ROWS_PER_PAGE + 1);
            q.setOffset(page * ROWS_PER_PAGE);
            QueryResult res = q.execute();
            NodeIterator nodes = res.getNodes();
            answer.writeAttribute(CURRENT, 1);
            answer.writeAttribute(MORE, nodes.getSize() > ROWS_PER_PAGE );
            answer.writeArray(DATA);
            long size = nodes.getSize();
            while(nodes.hasNext()) {
                Node node = nodes.nextNode();
                if (nodes.getPosition() < ROWS_PER_PAGE + 1) {
                    Recyclable r = resourceManagement.getRecyclable(request.getResourceResolver(), node.getPath());
                    answer.writeObject();
                    answer.writeAttribute(PATH, r.getResourcePath());
                    answer.writeAttribute(DELETED_BY, r.getDeletedBy());
                    answer.writeAttribute(DATE_DELETED, DELETED_DATE_FORMAT.format(r.getDeletedDate()));
                    answer.writeAttribute(RECYCLE_BIN, r.getResource().getPath());
                    answer.writeClose();
                }
            }
            answer.writeClose();
            return answer;
        } catch (RepositoryException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(FAILED_TO_LIST_RECYCLABLES).setException(e);
        }
    }
}

