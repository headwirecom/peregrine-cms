package com.peregrine.admin.servlets;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2026 headwire inc.
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

import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import org.osgi.service.component.annotations.Component;

/**
 * Given the full JCR path of a component being deleted, checks whether a node with the same
 * node name exists as a top-level component in any skeleton page of the same project.
 *
 * Matching is done by node name (not sling:resourceType), so only the exact component instance
 * shared across templates and skeleton pages is matched.
 *
 * "Same project" is determined by the tenant segment: /content/{tenant}/...
 * Only /content/{tenant}/pages/skeleton-pages/ is searched.
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "IsComponentUsedInSkeleton Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_PATHS + EQUALS + "/perapi/admin/isComponentUsedInSkeleton"
    }
)
@SuppressWarnings("serial")
public final class IsComponentUsedInSkeletonServlet extends AbstractBaseServlet {

    public static final String NO_PATH_PROVIDED = "No path provided";
    public static final String PATH_PARAM = "path";
    public static final String IS_TOP_LEVEL_IN_SKELETON = "isTopLevelInSkeleton";
    public static final String SKELETON_PAGES_KEY = "skeletonPages";
    public static final String PATH_KEY = "path";
    public static final String TITLE_KEY = "title";

    @Override
    protected Response handleRequest(final Request request) throws IOException {
        final String fullPath = request.getParameter(PATH_PARAM);
        if (isBlank(fullPath)) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(NO_PATH_PROVIDED);
        }

        final String nodeName = fullPath.contains("/")
            ? fullPath.substring(fullPath.lastIndexOf('/') + 1)
            : fullPath;
        if (isBlank(nodeName)) {
            return emptyResponse();
        }

        final String tenant = extractTenant(fullPath);
        if (tenant == null) {
            return emptyResponse();
        }

        final Session session = request.getResourceResolver().adaptTo(Session.class);
        if (session == null) {
            return emptyResponse();
        }

        final List<PageInfo> matchingPages = new ArrayList<>();
        try {
            final String skeletonPagesPath = "/content/" + tenant + "/pages/skeleton-pages";
            if (session.nodeExists(skeletonPagesPath)) {
                findTopLevelByName(session, nodeName, skeletonPagesPath, matchingPages);
            }
        } catch (RepositoryException e) {
            logger.error("Error checking skeleton usage for path: {}", fullPath, e);
        }

        final boolean used = !matchingPages.isEmpty();
        final JsonResponse response = new JsonResponse()
            .writeAttribute(IS_TOP_LEVEL_IN_SKELETON, used)
            .writeArray(SKELETON_PAGES_KEY);
        for (PageInfo info : matchingPages) {
            response.writeObject()
                .writeAttribute(PATH_KEY, info.path)
                .writeAttribute(TITLE_KEY, info.title)
                .writeClose();
        }
        return response.writeClose();
    }

    /**
     * Searches under skeletonPagesPath for nodes whose JCR name equals nodeName
     * and that are top-level (no ancestor with sling:resourceType between them and jcr:content).
     */
    private void findTopLevelByName(
        final Session session,
        final String nodeName,
        final String skeletonPagesPath,
        final List<PageInfo> result) throws RepositoryException {

        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        final String sql = "SELECT * FROM [nt:base] AS node "
            + "WHERE ISDESCENDANTNODE(node, '" + skeletonPagesPath.replace("'", "''") + "') "
            + "AND NAME(node) = '" + nodeName.replace("'", "''") + "'";
        final Query query = queryManager.createQuery(sql, Query.JCR_SQL2);
        final QueryResult queryResult = query.execute();
        final NodeIterator nodeIterator = queryResult.getNodes();

        while (nodeIterator.hasNext()) {
            final Node node = nodeIterator.nextNode();
            if (!isTopLevel(node)) {
                continue;
            }
            final PageInfo pageInfo = getContainingSkeletonPage(node);
            if (pageInfo != null && !isDuplicate(result, pageInfo.path)) {
                result.add(pageInfo);
            }
        }
    }

    /**
     * Returns true if no ancestor between the node and jcr:content has a sling:resourceType.
     */
    private boolean isTopLevel(final Node node) throws RepositoryException {
        Node current = node.getParent();
        while (current != null && current.getDepth() > 0) {
            if ("jcr:content".equals(current.getName())) {
                return true;
            }
            if (current.hasProperty(SLING_RESOURCE_TYPE)) {
                return false;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Walks up to find the per:Page node that represents the skeleton page.
     */
    private PageInfo getContainingSkeletonPage(final Node node) throws RepositoryException {
        Node current = node.getParent();
        while (current != null && current.getDepth() > 0) {
            if ("jcr:content".equals(current.getName())) {
                final Node page = current.getParent();
                if (PAGE_PRIMARY_TYPE.equals(page.getPrimaryNodeType().getName())) {
                    return new PageInfo(page.getPath(), getNodeTitle(page));
                }
                return null;
            }
            current = current.getParent();
        }
        return null;
    }

    private String getNodeTitle(final Node node) throws RepositoryException {
        if (node.hasNode("jcr:content")) {
            final Node content = node.getNode("jcr:content");
            if (content.hasProperty(JCR_TITLE)) {
                return content.getProperty(JCR_TITLE).getString();
            }
        }
        if (node.hasProperty(JCR_TITLE)) {
            return node.getProperty(JCR_TITLE).getString();
        }
        return node.getName();
    }

    /**
     * Extracts the tenant from /content/{tenant}/...
     * Returns null if the path does not have that structure.
     */
    private String extractTenant(final String path) {
        final String[] segments = path.split("/");
        if (segments.length >= 3 && "content".equals(segments[1]) && !segments[2].isEmpty()) {
            return segments[2];
        }
        return null;
    }

    private boolean isDuplicate(final List<PageInfo> list, final String path) {
        for (PageInfo info : list) {
            if (info.path.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private JsonResponse emptyResponse() throws IOException {
        return new JsonResponse()
            .writeAttribute(IS_TOP_LEVEL_IN_SKELETON, false)
            .writeArray(SKELETON_PAGES_KEY)
            .writeClose();
    }

    private static class PageInfo {
        final String path;
        final String title;

        PageInfo(final String path, final String title) {
            this.path = path;
            this.title = title;
        }
    }
}
