package com.peregrine.seo;

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.commons.util.PerUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.resource.filter.ResourcePredicates;
import org.apache.sling.resource.filter.ResourceStream;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = Servlet.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Web Robots Servlet",
        ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "="
            + PerConstants.ROBOTS_SERVLET_PATH,
        ServletResolverConstants.SLING_SERVLET_EXTENSIONS + "=" + PerConstants.TXT,
        ServletResolverConstants.SLING_SERVLET_METHODS + "=" + PerUtil.GET
    }
)
public final class RobotsServlet extends SlingAllMethodsServlet {

  private static final Logger log = LoggerFactory.getLogger(RobotsServlet.class);

  @Reference
  private ResourcePredicates resourceFilter;

  @Reference
  private UrlExternalizer externalizer;

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType(request.getResponseContentType());
    ResourceResolver resolver = request.getResourceResolver();
    PerPageManager pageManager = resolver.adaptTo(PerPageManager.class);
    PerPage parentPage = pageManager.getPage(request.getResource().getParent().getPath());

    PrintWriter writer = response.getWriter();
    writer.print("User-agent: *");
    writer.print("\n");

    writer.print("Allow: /");
    writer.print("\n");

    new ResourceStream(parentPage.adaptTo(Resource.class))
        .stream(r -> true)
        .filter(resourceFilter.parse(PerConstants.PAGE_RESOURCE_PREDICATE))
        .forEach(res -> {
          print(res.adaptTo(PerPage.class), writer, resolver);
        });

    new ResourceStream(parentPage.adaptTo(Resource.class))
        .stream(r -> true)
        .filter(resourceFilter.parse(PerConstants.SITEMAP_RESOURCE_PREDICATE))
        .forEach(res -> {
          String sitemapUrl = externalizer.buildExternalizedLink(resolver, String.format("%s.xml", res.getPath()));
          writer.print("Sitemap: " + sitemapUrl);
          writer.print("\n");
        });

    writer.flush();
  }

  private void print(PerPage page, PrintWriter writer, ResourceResolver resolver) {
    if (page.getContentProperty(PerConstants.DISALLOW_FROM_ROBOTS, false)) {
      String exclusionPage = externalizer
          .externalizeUrl(String.format("%s.html", page.getPath()), resolver, null);
      writer.print("Disallow: " + exclusionPage);
      writer.print("\n");
    }
  }
}
