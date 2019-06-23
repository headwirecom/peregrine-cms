package com.peregrine;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.getResource;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.CHANGES;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.resource.filter.ResourcePredicates;
import org.apache.sling.resource.filter.ResourceStream;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = ResourceChangeListener.class,
    configurationPolicy = ConfigurationPolicy.IGNORE,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Page Properties Rewriter",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        PATHS + EQUALS + "glob:" + "/content/sites" + "/**",
        CHANGES + EQUALS + "ADDED",
        CHANGES + EQUALS + "CHANGED",
        CHANGES + EQUALS + "REMOVED"
    }
)
public class PageListener implements ResourceChangeListener {

  private static final Logger log = LoggerFactory.getLogger(PageListener.class);

  @Reference
  ResourcePredicates resourceFilter;

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Override
  public void onChange(List<ResourceChange> changes) {
    if (changes != null) {
      for (final ResourceChange change : changes) {
        log.trace("Resource Change: '{}'", change);
        ResourceResolver resourceResolver = null;
        try {
          resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
          Resource perPage = getResource(resourceResolver, change.getPath());
          String primaryType = getPrimaryType(perPage);

//          if (PAGE_PRIMARY_TYPE.equals())



//          log.info(perPage.getPath());

        } catch (LoginException e) {
          log.error(e.getMessage());
        } finally {
          if (resourceResolver.isLive()) {
            resourceResolver.close();
          }
        }




//
//        switch (change.getType()) {
//          case ADDED: case CHANGED:
//
//            log.debug("Change Type ADDED: {}", change);
//            if (change.getAddedPropertyNames().contains("someProperty")) {
//              // Do some work
//              // In this case we will pass some some data from the Event to a custom job via a custom Job topic.
//              final Map<String, Object> props = new HashMap<String, Object>();
//              props.put("path", change.getPath());
//              props.put("userId", change.getUserId());
//              jobManager.addJob("com/adobe/acs/commons/samples/somePropertyAdded", props);
//            }
//            break;
//          case CHANGED:
//            log.debug("Change Type CHANGED: {}", change);
//            if (change.getChangedPropertyNames().contains("someOtherProperty")) {
//              // Do some other work
//            }
//            break;
//          case REMOVED:
//            log.debug("Change Type REMOVED: {}", change);
//            // etc.
//            break;
//          default:
//            break;
//        }



      }
    }
  }

//
//    if (changes != null) {
//      for (ResourceChange change : changes) {
//        ResourceResolver resourceResolver = null;
//        try {
//          resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
//          Resource page = getResource(resourceResolver, change.getPath());
//          String primaryType = getPrimaryType(page);
//
//          log.error(page.getPath());

//
//          if (PAGE_PRIMARY_TYPE.equals(primaryType)) {
//            ValueMap jcrContent = getModifiableProperties(page, true);
//            if (jcrContent != null) {
////              Object canonicalLink = jcrContent.get("canonicalLink");
////              if (canonicalLink )
//
//
//
//            }

//        } catch (LoginException e) {
//          log.error(e.getMessage());
//        }
//      }
//    }

//  Resource content = resource.getChild("jcr:content");
//  ModifiableValueMap resourcePath = content.adaptTo(ModifiableValueMap.class);
//        resourcePath.put("resourcePath", resource.getPath());
//
//        try {
//    content.getResourceResolver().commit();
//  } catch (PersistenceException e) {
//    e.printStackTrace();
//  }
//


  private void handlePropertyRewrite(final String sitesPath) {
    ResourceResolver resourceResolver = null;
    try {
      resourceResolver = resourceResolverFactory.getServiceResourceResolver(null);

      String query = "[" + JCR_PRIMARY_TYPE + "] is '" + PAGE_PRIMARY_TYPE + "'"
          + " and " + "[" + JCR_CONTENT + "/" + JCR_PRIMARY_TYPE + "] is '" + PAGE_CONTENT_TYPE
          + "'";

      List<Resource> sitePages = new ResourceStream(resourceResolver.getResource(sitesPath))
          .stream(r -> true)
          .filter(resourceFilter.parse(query))
          .collect(Collectors.toList());

      sitePages.forEach(res -> {

        log.info("Page -> " + res.getPath() + "  |  " + res.getName());

        ModifiableValueMap map = res.getResourceResolver().getResource(res, "jcr:content")
            .adaptTo(ModifiableValueMap.class);
        map.put("canonicalLink", res.getPath());
        try {
          res.getResourceResolver().getResource(res, "jcr:content").getResourceResolver().commit();
        } catch (PersistenceException e) {
          e.printStackTrace();
        }
        log.info("Content -> " + res.getResourceResolver().getResource(res, "jcr:content").getPath()
            + "  |  " + res.getResourceResolver().getResource(res, "jcr:content").getName());

      });

    } catch (LoginException e) {
      log.error("Can not obtain resource resolver '{}'", e.getMessage());
    } finally {
      if (resourceResolver != null) {
        resourceResolver.close();
      }
    }
  }
}