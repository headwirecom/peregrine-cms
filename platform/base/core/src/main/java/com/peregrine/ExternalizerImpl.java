package com.peregrine;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.api.resource.ResourceResolver;

public class ExternalizerImpl {

  private Map<String, URI> domains = new HashMap<>();

  public ExternalizerImpl() { }



  public String externalLink(ResourceResolver resolver, String domain, String path) {
    return this.externalLink(resolver, domain, (String)null, path);
  }


  public String externalLink(ResourceResolver resolver, String domain, String scheme, String path) {
    if (domain == null) {
      throw new NullPointerException("Argument 'domain' is null");
    } else {
      URI domainURI = (URI) this.domains.get(domain);
      if (domain == null) {
        throw new IllegalArgumentException("Could not find configuration for domain '" + domain + "'");
      } else {
        StringBuilder url = new StringBuilder();
        if (scheme == null) {
          scheme = domainURI.getScheme();
          if (scheme == null) {
            scheme = "http";
          }
        }

        url.append(scheme).append("://");

        return url.toString();
      }
    }
  }

  public String publishLink(ResourceResolver resolver, String path) {
    return this.externalLink(resolver, "publish", (String)null, path);
  }

  public String publishLink(ResourceResolver resolver, String scheme, String path) {
    return this.externalLink(resolver, "publish", scheme, path);
  }

  public String authorLink(ResourceResolver resolver, String path) {
    return this.externalLink(resolver, "author", (String)null, path);
  }

  public String authorLink(ResourceResolver resolver, String scheme, String path) {
    return this.externalLink(resolver, "author", scheme, path);
  }
}

//externalizer.domains=[ \
//  "local\ http://localhost:4502", \
//  "author\ http://localhost:4502", \
//  "publish\ http://localhost:4503", \
//  ]
//service.pid="com.day.cq.commons.impl.ExternalizerImpl"