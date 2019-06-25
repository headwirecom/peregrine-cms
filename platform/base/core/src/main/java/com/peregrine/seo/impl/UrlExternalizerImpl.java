package com.peregrine.seo.impl;

import static com.peregrine.commons.util.PerConstants.AUTHOR_RUN_MODE;
import static com.peregrine.commons.util.PerConstants.DEFAULT_PROTOCOL;
import static com.peregrine.commons.util.PerConstants.PUBLISH_RUN_MODE;
import static com.peregrine.commons.util.PerConstants.STANDALONE;

import com.peregrine.seo.UrlExternalizer;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    service = UrlExternalizer.class,
    immediate = true
)
@Designate(ocd = UrlExternalizerImpl.Configuration.class)
public class UrlExternalizerImpl implements UrlExternalizer {

  @ObjectClassDefinition(
      name = "Peregrine: UrlExternalizer",
      description = "UrlExternalizer to externalize URLs."
  )
  @interface Configuration {

    @AttributeDefinition(
        name = "percms.domains",
        description = "List of domain mappings.",
        required = true
    )
    String[] domains() default {"local http://localhost:8080", "author http://localhost:8080",
        "publish http://localhost:8180"};
  }

  private static final Logger log = LoggerFactory.getLogger(UrlExternalizerImpl.class);

  private static final String DEFAULT_LOCAL_DOMAIN = "local http://localhost:8080";
  private static final String DEFAULT_AUTHOR_DOMAIN = "author http://localhost:8080";
  private static final String DEFAULT_PUBLISH_DOMAIN = "publish http://localhost:8180";

  private static final String[] DEFAULT_DOMAINS = new String[]{
      DEFAULT_LOCAL_DOMAIN,
      DEFAULT_AUTHOR_DOMAIN,
      DEFAULT_PUBLISH_DOMAIN
  };

  private Map<String, URI> domains = new HashMap<>();

  @Reference
  private SlingSettingsService slingSettingsService;

  @Override
  public String externalizeUrl(String path, ResourceResolver resolver, SlingHttpServletRequest request) {
    String actualPath = path;
    String urlRemainder = null;
    int urlRemainderPos = StringUtils.indexOfAny(actualPath, '?', '#');
    if (urlRemainderPos >= 0) {
      urlRemainder = actualPath.substring(urlRemainderPos);
      actualPath = actualPath.substring(0, urlRemainderPos);
    }
    actualPath = Objects.nonNull(request) ? resolver.map(request, actualPath) : resolver.map(actualPath);
    try {
      actualPath = new URI(actualPath).getRawPath();
      actualPath = StringUtils.replace(actualPath, "%2F", "/");
    } catch (URISyntaxException ex) {
      throw new RuntimeException("Sling map method returned invalid URI: " + actualPath, ex);
    }
    return Objects.isNull(actualPath) ? null : actualPath + (urlRemainder != null ? urlRemainder : "");
  }

  @Override
  public String externalizeUrlWithoutMapping(String path, SlingHttpServletRequest request) {
    String actualPath = path;
    String urlRemainder = null;
    int urlRemainderPos = StringUtils.indexOfAny(actualPath, '?', '#');
    if (urlRemainderPos >= 0) {
      urlRemainder = actualPath.substring(urlRemainderPos);
      actualPath = actualPath.substring(0, urlRemainderPos);
    }
    actualPath = mangleNamespaces(actualPath);
    if (request != null) {
      actualPath = StringUtils.defaultString(request.getContextPath()) + actualPath; //NOPMD
    }
    try {
      actualPath = URLEncoder.encode(actualPath, "UTF-8");
    } catch (UnsupportedEncodingException var2) {
      throw new RuntimeException(var2);
    }
    actualPath = StringUtils.replace(actualPath, "+", "%20");
    actualPath = StringUtils.replace(actualPath, "%2F", "/");
    return actualPath + (urlRemainder != null ? urlRemainder : "");
  }

  @Override
  public String buildExternalizedLink(ResourceResolver resolver, String path) {
    String domain = null;
    if (slingSettingsService != null) {
      if (slingSettingsService.getRunModes().contains(PUBLISH_RUN_MODE)) {
        domain = PUBLISH_RUN_MODE;
      } else if (slingSettingsService.getRunModes().contains(AUTHOR_RUN_MODE)) {
        domain = AUTHOR_RUN_MODE;
      } else {
        domain = STANDALONE;
      }
    }
    URI domainURI = (URI) this.domains.get(domain);
    Objects.requireNonNull(domainURI, "No configuration for domain '" + domain + "' exists.");

    StringBuilder url = new StringBuilder();

    String scheme = Objects.isNull(domainURI.getScheme()) ? "http" : domainURI.getScheme();
    url.append(scheme).append("://");
    String domainAuthority =
        domainURI.getPort() > 0
            && (!"http".equals(scheme) || domainURI.getPort() != 80)
            && (!"https".equals(scheme) || domainURI.getPort() != 443)
            ? domainURI.getHost() + ":" + domainURI.getPort() : domainURI.getHost();
    URI slingMapped = URI.create(this.externalizeUrl(path, resolver, null));
    String authority =
        Objects.isNull(slingMapped.getAuthority()) ? domainAuthority : slingMapped.getAuthority();
    url.append(authority);
    if (Objects.nonNull(domainURI.getRawPath())) {
      url.append(domainURI.getRawPath());
    }
    url.append(slingMapped.getRawPath());
    if (Objects.nonNull(slingMapped.getRawQuery())) {
      url.append("?").append(slingMapped.getRawQuery());
    }
    if (Objects.nonNull(slingMapped.getRawFragment())) {
      url.append("#").append(slingMapped.getRawFragment());
    }
    log.debug("externalizing link for '{}': {} -> {}", domain, path, url);
    return url.toString();
  }

  private static final Pattern EXTERNALIZED_PATTERN = Pattern.compile("^([^/]+:|//|#).*$");

  @Override
  public boolean isExternalized(String url) {
    if (url == null) {
      log.error("Given url cannot be null!");
      return false;
    }
    return EXTERNALIZED_PATTERN.matcher(url).matches();
  }

  private static final String MANGLED_NAMESPACE_PREFIX = "/_";
  private static final String MANGLED_NAMESPACE_SUFFIX = "_";
  private static final char NAMESPACE_SEPARATOR = ':';
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("/([^:/]+):");

  @Override
  public String mangleNamespaces(String path) {
    if (path == null) {
      log.error("Given path cannot be null!");
      return null;
    } else {
      Matcher matcher = NAMESPACE_PATTERN.matcher(path);
      StringBuffer sb = new StringBuffer();
      while (matcher.find()) {
        String replacement = MANGLED_NAMESPACE_PREFIX + matcher.group(1) + MANGLED_NAMESPACE_SUFFIX;
        matcher.appendReplacement(sb, replacement);
      }
      matcher.appendTail(sb);
      return sb.toString();
    }
  }

  @Activate
  @SuppressWarnings("unused")
  void activate(Configuration configuration) {
    setup(configuration);
  }

  @Modified
  @SuppressWarnings("unused")
  void modified(Configuration configuration) {
    setup(configuration);
  }

  private void setup(Configuration configuration) {
    configuration.domains();
    String[] domainConfigs =
        configuration.domains() != null ? configuration.domains() : DEFAULT_DOMAINS;
    for (int i = 0; i < domainConfigs.length; ++i) {
      String var1 = domainConfigs[i];
      var1 = var1.trim();
      if (var1.indexOf(32) > 0) {
        String n = var1.substring(0, var1.indexOf(32));
        try {
          String d = var1.substring(var1.indexOf(32) + 1);
          if (!d.contains("://")) {
            d = DEFAULT_PROTOCOL + d;
          }
          domains.put(n, URI.create(d));
        } catch (Exception e) {
          log.error(e.getLocalizedMessage());
        }
      }
    }
  }
}
