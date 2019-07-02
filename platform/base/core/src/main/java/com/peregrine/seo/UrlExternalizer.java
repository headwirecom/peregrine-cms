package com.peregrine.seo;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Service interface to externalize URLs.
 */
public interface UrlExternalizer {

  /**
   * Externalizes an URL by applying Sling Mapping. Hostname and scheme are not added because they
   * are added by the link handler depending on site URL configuration and secure/non-secure mode.
   * URLs that are already externalized remain untouched.
   *
   * @param path Unexternalized URL (without scheme or hostname)
   * @param resolver Resource resolver
   * @param request Request
   * @return Exernalized URL without scheme or hostname, but with short URLs (if configured in Sling
   * Mapping is configured), and the path is URL-encoded if it contains special chars.
   */
  String externalizeUrl(String path, ResourceResolver resolver, SlingHttpServletRequest request);

  /**
   * Externalizes an URL without applying Sling Mapping. Instead the servlet context path is added and sling namespace
   * mangling is applied manually.
   * Hostname and scheme are not added because they are added by the link handler depending on site URL configuration
   * and secure/non-secure mode. URLs that are already externalized remain untouched.
   * @param path Unexternalized URL (without scheme or hostname)
   * @param request Request
   * @return Exernalized URL without scheme or hostname, the path is URL-encoded if it contains special chars.
   */
  String externalizeUrlWithoutMapping(String path, SlingHttpServletRequest request);

  /**
   * Build externalized URL that links to a content page.
   * @param resolver a resource resolver for handling the sling mappings and namespace mangling; can be null
   * @param path a resource path; might contain extension, query or fragment, but plain paths are recommended; has to be without context path
   * @return an absolute URL string
   */
  String buildExternalizedLink(ResourceResolver resolver, String path);

  /**
   * Checks if the given URL is already externalized. For this check some heuristics are applied.
   *
   * @param url URL
   * @return true if path is already externalized.
   */
  boolean isExternalized(String url);

  /**
   * Mangle the namespaces in the given path for usage in sling-based URLs.
   * <p>
   * E.g. /path/jcr:content -> /path/_jcr_content
   * </p>
   *
   * @param path Path to mangle
   * @return Mangled path
   */
  String mangleNamespaces(String path);
}

