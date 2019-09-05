package com.peregrine.blog.servlets;

/**
 * Created by Andreas Schaefer on 7/18/17.
 */

import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;
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
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "perapi/blog/blogPosts"
    }
)
@SuppressWarnings("serial")
public class BlogListServlet
    extends AbstractBaseServlet
{
    public static final String BLOG_POST_RESOURCE_TYPE = "blog/objects/post";
    public static final String BLOG_OBJECTS_PATH = "/content/objects/blog/posts";
    public static final String BLOG_ROLL_TYPE = "roll";
    public static final String BLOG_MONTHLY_TYPE = "monthly";
    public static final String BLOG_MOST_VISITED_TYPE = "hits";

    @Override
    protected Response handleRequest(Request request) throws IOException {
        JsonResponse answer = null;
        String parentPath = request.getParameter("path", BLOG_OBJECTS_PATH);
        Resource parent = getResource(request.getResourceResolver(), parentPath);
        if(parent == null) {
            return new ErrorResponse().setErrorMessage("Parent Path does not point to a resource").setRequestPath(parentPath);
        }
        if(!parentPath.endsWith("/posts")) {
            Resource posts = parent.getChild("posts");
            if(isPrimaryType(posts, SLING_ORDERED_FOLDER)) {
                parent = posts;
            }
        }
        String type = request.getParameter("type", BLOG_ROLL_TYPE);
        int page = request.getIntParameter("page", 1);
        int pageSize = request.getIntParameter("pageSize", 10);
        answer = new JsonResponse()
            .writeAttribute("type", type)
            .writeAttribute("page", page)
            .writeAttribute("pageSize", pageSize)
            .writeArray("children");
        boolean more = false;
        if(BLOG_ROLL_TYPE.equals(type)) {
            int skip = (page - 1) * pageSize;
            int read = pageSize;
            for(Resource child : parent.getChildren()) {
                logger.debug("Handle child: '{}', read: '{}', skip: '{}'", child, read, skip);
                if(skip > 0) {
                    logger.debug("Skip Child: '{}'", skip);
                    skip--;
                } else {
                    ValueMap properties = child.getValueMap();
                    String resourceType = properties.get(SLING_RESOURCE_TYPE, String.class);
                    if(isEmpty(resourceType) || !BLOG_POST_RESOURCE_TYPE.equals(resourceType)) {
                        logger.debug("Unexpected Resource Type: '{}'", resourceType);
                        continue;
                    }
                    if(read <= 0) {
                        logger.debug("Done with reading");
                        more = true;
                        break;
                    }
                    String author = properties.get("author", "unknown");
                    Resource authorResource = getResource(request.getResourceResolver(), author);
                    if(authorResource != null) {
                        author = authorResource.getValueMap().get("name", author);
                    }
                    answer.writeObject()
                        .writeAttribute("name", child.getName())
                        .writeAttribute("path", child.getPath())
                        .writeAttribute("title", properties.get("title", ""))
                        .writeAttribute("date", properties.get("date",""))
                        .writeAttribute("author", author)
                        .writeAttribute("lead", properties.get("lead", ""))
                        .writeAttribute("text", properties.get("text", ""))
                        .writeClose();
                    read--;
                }
            }
        }
        answer.writeClose();
        answer.writeAttribute("more", more);
        return answer;
    }
}
