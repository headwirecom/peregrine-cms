package com.peregrine.nodetypes;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.resource.filter.ResourcePredicates;
import org.apache.sling.resource.filter.ResourceStream;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class PagePropertiesNodeType {

  private static final Logger log = LoggerFactory.getLogger(PagePropertiesNodeType.class);
  private static final String SITES_ROOT_PATH = "/content/sites";

  private Resource resource;

  @Reference
  ResourcePredicates resourceFilter;

  @Reference
  ResourceResolverFactory resourceResolverFactory;

  @Activate
  public void activate(ComponentContext context) {
    ResourceResolver resourceResolver = null;
    try {
      resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

//      Resource rootResource = resourceResolver.getResource(resourcePath);
//      ResourceFilterStream resourceStream = rootResource.adaptTo(ResourceFilterStream.class);
//      resourceStream
//          .setBranchSelector("[" + JCR_PRIMARY_TYPE + "] is '" + PAGE_PRIMARY_TYPE + "'")
//          .setChildSelector("[" + JCR_CONTENT + "/" + JCR_PRIMARY_TYPE + "] is '" + PAGE_CONTENT_TYPE + "'")
//          .stream()
//          .forEach(r -> {
//            log.info(r.getPath());
//          });

      resource = resourceResolver.getResource(SITES_ROOT_PATH);

      String query = new StringBuilder()
          .append("[" + JCR_PRIMARY_TYPE + "] is '" + PAGE_PRIMARY_TYPE + "'")
          .append(" and ")
          .append("[" + JCR_CONTENT + "/" + JCR_PRIMARY_TYPE + "] is '" + PAGE_CONTENT_TYPE + "'")
          .toString();
      List<Resource> resources = handle(SITES_ROOT_PATH, query);

      resources.forEach(res -> {


//        ModifiableValueMap map = res.adaptTo(ModifiableValueMap.class);
//        map.put("canonical_link", res.getPath());
//        try {
//          res.getResourceResolver().commit();
//        } catch (PersistenceException e) {
//          log.error(e.getMessage());
//        }
        log.info("Page -> " + res.getPath() + "  |  " + res.getName());

        ModifiableValueMap map = res.getResourceResolver().getResource(res, "jcr:content").adaptTo(ModifiableValueMap.class);
        map.put("canonicalLink", res.getPath());
        try {
          res.getResourceResolver().getResource(res, "jcr:content").getResourceResolver().commit();
        } catch (PersistenceException e) {
          e.printStackTrace();
        }
        log.info("Content -> " + res.getResourceResolver().getResource(res, "jcr:content").getPath() + "  |  " + res.getResourceResolver().getResource(res, "jcr:content").getName());
      });

    } catch (Exception e) {
      log.error(e.getMessage());
    } finally {
      resourceResolver.close();
    }
  }

//  public String getPagePath(final String resourcePath) {
//    ResourceResolver resourceResolver = null;
//    try {
//      resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
//      final PerPageManager pageManager = factory.getAdapter(resourceResolver, PerPageManager.class);
//      final PerPage containingPage = pageManager.getPage(resourcePath);
//      if (containingPage != null) {
//        return containingPage.getPath();
//      }
//    } catch (LoginException e) {
//      log.error("Can not obtain resource resolver: '{}'", e.getMessage());
//    } finally {
//      if (resourceResolver != null) {
//        resourceResolver.close();
//      }
//    }
//    return null;
//  }

  private List<Resource> handle(String path, String filter) {
    return new ResourceStream(resource)
        .stream(r -> true)
        .filter(resourceFilter.parse(filter))
        .collect(Collectors.toList());
  }

//  private static void traverseChildren(Resource parent) {
//    for (Resource child : parent.getChildren()) {
//      String perPage = PAGE_PRIMARY_TYPE;
//      if (perPage.equals(child.getResourceType())) {
//        log.info(child.getPath());
////        Resource jcrContent = child.getChild(JCR_CONTENT);
////        Map<String, String> perPageContent = new HashMap<>();
////
////        if (jcrContent != null && perPageContent.equals(jcrContent.getValueMap(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE)) {
////          log.info(r.getPath());
////        }
//      }
//      traverseChildren(child);
//    }
//  }
}