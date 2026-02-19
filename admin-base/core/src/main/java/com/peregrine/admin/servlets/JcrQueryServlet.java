package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import javax.servlet.Servlet;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.NodeIterator;
import javax.jcr.Node;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/ai/query",
        "sling.servlet.methods=GET"
})
public class JcrQueryServlet extends AbstractBaseServlet {

    protected Response handleRequest(Request request) throws IOException {
        try {
            String queryStr = request.getParameter("statement");
            final JsonResponse answer = new JsonResponse();
            // 1. Execute JCR-SQL2
            Query query = request.getResourceResolver().adaptTo(javax.jcr.Session.class)
                    .getWorkspace().getQueryManager().createQuery(queryStr, Query.JCR_SQL2);
            QueryResult result = query.execute();
            NodeIterator nodes = result.getNodes();


            answer.writeArray("results");
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                answer.writeObject(node.getPath());
                answer.writeAttribute("title", node.hasProperty("jcr:title") ? node.getProperty("jcr:title").getString() : "");
                answer.writeClose();
            }
            answer.writeClose();
            return answer;

        } catch (Exception e) {
            return new ErrorResponse().setException(e);
        }
    }
}