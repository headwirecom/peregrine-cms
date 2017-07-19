package com.blog.servlets;

/**
 * Created by schaefa on 7/18/17.
 */

import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;

import java.io.IOException;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Blog List Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "GET",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/blog/blogPosts"
    }
)
@SuppressWarnings("serial")
public class BlogListServlet
    extends AbstractBaseServlet
{
    @Override
    protected Response handleRequest(Request request) throws IOException {
        JsonResponse answer = null;
        String parentPath = request.getParameter("path");
//        try {
//            Resource newPage = resourceManagement.createPage(request.getResourceResolver(), parentPath, name, templatePath);
//            request.getResourceResolver().commit();
            answer = new JsonResponse()
                .writeAttribute("type", "roll")
                .writeAttribute("page", 1)
                .writeArray("children");
//                .writeArray("__children__");
            // Loop
                answer.writeObject()
                    .writeAttribute("name", "post-1")
//                    .writeAttribute("sling:resourceType", "blog/objects/post")
                    .writeAttribute("title", "Post 1")
                    .writeAttribute("date", "2017-07-18")
                    .writeAttribute("author", "Andreas Schaefer")
                    .writeAttribute("lead", "Post 1 Lead here")
                    .writeAttribute("text", "Post 1 Content here")
                    .writeClose();
            // End of Loop
            answer.writeClose();
//        } catch (ManagementException e) {
//            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to create page").setRequestPath(parentPath).setException(e);
//        }
        return answer;
    }
}
