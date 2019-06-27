package com.peregrine.seo;

//import static com.peregrine.commons.util.PerConstants.CHANGE_FREQ;
//import static com.peregrine.commons.util.PerConstants.DEFAULT_CHANGEFREQ;
//import static com.peregrine.commons.util.PerConstants.DEFAULT_PRIORITY

import static com.peregrine.commons.util.PerConstants.EXCLUDE_FROM_SITEMAP;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PRIORITY;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_EXTENSIONS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

import com.peregrine.adaption.Filter;
import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
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
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.resource.filter.ResourcePredicates;
import org.apache.sling.resource.filter.ResourceStream;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Page Site Map Servlet",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "per/Page",
        SLING_SERVLET_SELECTORS + EQUALS + "sitemap",
        SLING_SERVLET_EXTENSIONS + EQUALS + "xml",
        SLING_SERVLET_METHODS + EQUALS + GET
    }
)
public final class SiteMapServlet extends SlingAllMethodsServlet {

  private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
  private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

  @Reference
  private ResourcePredicates resourceFilter;

  @Reference
  private UrlExternalizer urlExternalizer;

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType(request.getResponseContentType());

    ResourceResolver resolver = request.getResourceResolver();
    PerPageManager pageManager = resolver.adaptTo(PerPageManager.class);
    PerPage page = pageManager.getPage(request.getResource().getPath());

    XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
    try {
      XMLStreamWriter stream = outputFactory.createXMLStreamWriter(response.getWriter());
      stream.writeStartDocument("1.0");

      stream.writeStartElement("", "urlset", NS);
      stream.writeNamespace("", NS);

      StringBuilder query = new StringBuilder();
      query.append("[" + JCR_PRIMARY_TYPE + "] is '" + PAGE_PRIMARY_TYPE + "'")
          .append(" and ")
          .append("[" + JCR_CONTENT + "/" + JCR_PRIMARY_TYPE + "] is '" + PAGE_CONTENT_TYPE + "'");

      new ResourceStream(page.adaptTo(Resource.class))
          .stream(r -> true)
          .filter(resourceFilter.parse(query.toString()))
          .forEach(res -> {
            try {
              write(res.adaptTo(PerPage.class), stream, resolver);
            } catch (XMLStreamException e) {
              e.printStackTrace();
            }
          });

      stream.writeEndElement();
      stream.writeEndDocument();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @SuppressWarnings("squid:S1192")
  private void write(PerPage page, XMLStreamWriter stream, ResourceResolver resolver)
      throws XMLStreamException {
    if (isHidden(page)) {
      return;
    }
    stream.writeStartElement(NS, "url");
    String loc = urlExternalizer
        .buildExternalizedLink(resolver, String.format("%s.html", page.getPath()));

    writeElement(stream, "loc", loc);
    Calendar cal = page.getLastModified();
    if (cal != null) {
      writeElement(stream, "lastmod", DATE_FORMAT.format(cal));
    }
    writeElement(stream, "changefreq", page.getContentProperty("changefreq", "weekly"));
    writeElement(stream, "priority",
        page.getContentProperty(PRIORITY, String.valueOf(0.5)));

    stream.writeEndElement();
  }

  private boolean isHidden(final PerPage page) {
    return page.getContentProperty(EXCLUDE_FROM_SITEMAP, false);
  }

  private void writeElement(final XMLStreamWriter stream, final String elementName,
      final String text)
      throws XMLStreamException {
    stream.writeStartElement(NS, elementName);
    stream.writeCharacters(text);
    stream.writeEndElement();
  }
}
