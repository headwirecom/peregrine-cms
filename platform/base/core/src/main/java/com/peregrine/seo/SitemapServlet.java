package com.peregrine.seo;

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.commons.util.PerUtil;
import java.io.IOException;
import java.util.Calendar;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
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
        Constants.SERVICE_DESCRIPTION + "=Page Site Map Servlet",
        ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "="
            + PerConstants.SITEMAP_SERVLET_PATH,
        ServletResolverConstants.SLING_SERVLET_EXTENSIONS + "=" + PerConstants.XML,
        ServletResolverConstants.SLING_SERVLET_EXTENSIONS + "=" + PerConstants.HTML,
        ServletResolverConstants.SLING_SERVLET_METHODS + "=" + PerUtil.GET
    }
)
public final class SitemapServlet extends SlingAllMethodsServlet {

  private static final Logger log = LoggerFactory.getLogger(SitemapServlet.class);

  private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
  private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

  @Reference
  private ResourcePredicates resourceFilter;

  @Reference
  private UrlExternalizer urlExternalizer;

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    if (PerConstants.HTML.equals(request.getRequestPathInfo().getExtension())) {
      Resource redirectPage = request.getResource().getParent();
      if (PerConstants.PAGE_PRIMARY_TYPE.equals(PerUtil.getPrimaryType(redirectPage))) {
        log.info("Sitemap HTML rendering is not supported. Redirecting to parent page: '{}'",
            redirectPage.getPath());
        response.sendRedirect(redirectPage.getPath() + ".html");
      }
    }

    response.setContentType(request.getResponseContentType());
    ResourceResolver resolver = request.getResourceResolver();
    PerPageManager pageManager = resolver.adaptTo(PerPageManager.class);
    PerPage page = pageManager.getPage(request.getResource().getParent().getPath());

    XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
    try {
      XMLStreamWriter stream = outputFactory.createXMLStreamWriter(response.getWriter());
      stream.writeStartDocument("1.0");
      stream.writeStartElement("", "urlset", NS);
      stream.writeNamespace("", NS);

      new ResourceStream(page.adaptTo(Resource.class))
          .stream(r -> true)
          .filter(resourceFilter.parse(PerConstants.PAGE_RESOURCE_PREDICATE))
          .forEach(res -> {
            try {
              write(res.adaptTo(PerPage.class), stream, resolver);
            } catch (XMLStreamException e1) {
              log.error(e1.getMessage());
            }
          });

      stream.writeEndElement();
      stream.writeEndDocument();

    } catch (XMLStreamException e2) {
      log.error(e2.getMessage());
      throw new IOException(e2);
    }
  }

  private void write(PerPage page, XMLStreamWriter stream, ResourceResolver resolver)
      throws XMLStreamException {
    if (isHidden(page)) {
      return;
    }
    stream.writeStartElement(NS, "url");

    String loc = urlExternalizer
        .buildExternalizedLink(resolver, String.format("%s.html", page.getPath()));
    writeElement(stream, "loc", loc);

    Calendar lastModified = null;
    if (page.getLastModified() != null) {
      lastModified = page.getLastModified();
    } else {
      lastModified = ResourceUtil.getValueMap(page.adaptTo(Resource.class))
          .get(PerConstants.CREATED, Calendar.getInstance());
    }
    writeElement(stream, "lastmod", DATE_FORMAT.format(lastModified));

    String changeFreq = page.getContentProperty(PerConstants.CHANGE_FREQUENCY, "weekly");
    writeElement(stream, PerConstants.CHANGE_FREQUENCY, changeFreq);

    String priority = page.getContentProperty(PerConstants.PRIORITY, "0.5");
    writeElement(stream, PerConstants.PRIORITY, priority);

    stream.writeEndElement();
  }

  private boolean isHidden(final PerPage page) {
    return page.getContentProperty(PerConstants.EXCLUDE_FROM_SITEMAP, false);
  }

  private void writeElement(final XMLStreamWriter stream, final String elementName,
      final String text) throws XMLStreamException {
    stream.writeStartElement(NS, elementName);
    stream.writeCharacters(text);
    stream.writeEndElement();
  }
}
