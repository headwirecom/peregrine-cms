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
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=search servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES +"=api/admin/search"
        }
)
@SuppressWarnings("serial")
public class RestrictedSearchServlet extends SlingSafeMethodsServlet {

    private static final long ROWS_PER_PAGE = 100;
    private final Logger log = LoggerFactory.getLogger(RestrictedSearchServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        String suffix = request.getRequestPathInfo().getSuffix();
        Resource res = request.getResource();
        String type = res.getValueMap().get("type", String.class);
        if("components".equals(type)) {
            findComponents(request, response);
        } else if("templates".equals(type)) {
            findTemplates(request, response);
        } else if("objects".equals(type)) {
            findObjects(request, response);
        }

    }

    private void findObjects(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        String query = "select * from per:ObjectDefinition order by jcr:path";
        findAndOutputToWriterAsJSON(request, response, query);
    }

    private void findComponents(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        String query = "select * from per:Component order by jcr:path";
        findAndOutputToWriterAsJSON(request, response, query);
    }

    private void findTemplates(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        String query = "select * from per:Page where jcr:path like '/content/templates/%' order by jcr:path";
        findAndOutputToWriterAsJSON(request, response, query);
    }

    private void findAndOutputToWriterAsJSON(SlingHttpServletRequest request, SlingHttpServletResponse response, String query) throws IOException {
        Writer w = response.getWriter();
        if(query.length() == 0) {
            noInput(response, query, w);
        } else {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            try {
                response.setContentType("application/json");

                if (query != null && query.trim().length() > 0) {
                    QueryManager qm = session.getWorkspace().getQueryManager();
                    Query q = qm.createQuery(query, Query.SQL);
                    q.setLimit(ROWS_PER_PAGE+1);
                    String pageParam = request.getParameter("page");
                    int page = 0;
                    if(pageParam != null) {
                        page = Integer.parseInt(pageParam);
                    }
                    q.setOffset(page*ROWS_PER_PAGE);

                    QueryResult res = q.execute();
                    NodeIterator nodes = res.getNodes();
                    w.write("{");
                    w.write("\"current\": "+"1"+",");
                    w.write("\"more\": "+(nodes.getSize() > ROWS_PER_PAGE)+",");
                    w.write("\"data\": [");
                    while(nodes.hasNext()) {
                        Node node = nodes.nextNode();
                        w.write("{ \"name\": \""+node.getName()+"\",");
                        w.write("\"path\": \""+node.getPath()+"\",");
                        if(node.getPrimaryNodeType().toString().equals("per:Component")) {
                            if(node.hasProperty("group")) {
                                Property group = node.getProperty("group");
                                if(group != null) {
                                    w.write("\"group\": \""+group.getString()+"\",");
                                }
                            }
                        }
                        w.write("\"nodeType\": \""+node.getPrimaryNodeType()+"\"}");
                        if(nodes.hasNext()) {
                            w.write(',');
                        }
                    }
                    w.write("]");
                    w.write("}");
                }
            } catch(Exception e) {
                log.error("not able to get query manager",e);
            }
        }
    }

    private void noInput(SlingHttpServletResponse response, String query, Writer w) throws IOException {
        w.write("{");
        w.write("\"current\": 1,");
        w.write("\"more\": fasle,");
        w.write("\"data\": [");
        w.write("]");
        w.write("}");
    }


}

