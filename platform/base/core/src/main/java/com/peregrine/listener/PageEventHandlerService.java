package com.peregrine.listener;

import static com.peregrine.commons.util.PerConstants.CANONICAL_LINK_ELEMENT;
import static com.peregrine.commons.util.PerConstants.DEFAULT_HOSTNAME;
import static com.peregrine.commons.util.PerConstants.DEFAULT_PROTOCOL;
import static com.peregrine.commons.util.PerConstants.EXCLUDE_FROM_NAVIGATION;
import static com.peregrine.commons.util.PerConstants.HOSTNAME;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PROTOCOL;
import static com.peregrine.commons.util.PerConstants.RESOURCE_CHANGE_LISTENER;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.CHANGES;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.util.PerUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.resource.filter.ResourcePredicates;
import org.apache.sling.resource.filter.ResourceStream;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = ResourceChangeListener.class,
    configurationPolicy = ConfigurationPolicy.IGNORE,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Page Properties Rewriter",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        PATHS + EQUALS + "glob:" + SITES_ROOT + "/**",
        CHANGES + EQUALS + "ADDED",
        CHANGES + EQUALS + "CHANGED",
        CHANGES + EQUALS + "REMOVED"
    }
)
public class PageEventHandlerService implements ResourceChangeListener {

  private static final Logger log = LoggerFactory.getLogger(PageEventHandlerService.class);

  private ResourceResolverFactory resourceResolverFactory;

  @Reference(
      cardinality = ReferenceCardinality.MULTIPLE,
      policy = ReferencePolicy.DYNAMIC,
      policyOption = ReferencePolicyOption.GREEDY
  )
  void bindResourceResolverFactory(ResourceResolverFactory factory) {
    log.trace("Bind ResourceResolverFactory: '{}'", factory);
    this.resourceResolverFactory = factory;
  }

  void unbindResourceResolverFactory(ResourceResolverFactory factory) {
    log.trace("Unbind ResourceResolverFactory: '{}'", factory);
    this.resourceResolverFactory = null;
  }

  @Reference
  private ResourcePredicates resourceFilter;

  @Override
  public void onChange(final List<ResourceChange> changes) {
    if (changes != null) {
      for (ResourceChange change : changes) {
        log.trace("Resource Change: '{}'", change);
        ResourceResolver resourceResolver = null;

        try {
          resourceResolver = PerUtil
              .loginService(resourceResolverFactory, RESOURCE_CHANGE_LISTENER);
          Resource resource = PerUtil.getResource(resourceResolver, change.getPath());
          String primaryType = PerUtil.getPrimaryType(resource);
          switch (change.getType()) {
            case ADDED:
              log.debug("Change Type ADDED: {}", change);
              if (PAGE_PRIMARY_TYPE.equals(primaryType)) {
                handlePages(resource);
              }
              break;
            case CHANGED:
              log.debug("Change Type CHANGED: {}", change);
              try {
                handleProperties(resource, PAGE_PRIMARY_TYPE.equals(primaryType));
              } catch (PersistenceException e) {
                log.error("Error: '{}', At Resource Path: '{}', Property Name: '{}'",
                    e.getMessage(), e.getResourcePath(), e.getPropertyName());
              }
              break;
            case REMOVED:
              log.debug("Change Type REMOVED: {}", change);
              break;
            default:
          }
        } catch (LoginException e) {
          log.error("Exception allocating resource resolver", e);
        } catch (RuntimeException e) {
          log.error("Unexpected Exception: '{}'", e.getMessage());
        } finally {
          if (resourceResolver != null) {
            resourceResolver.close();
          }
        }
      }
    }
  }

  /**
   * Handler for ChangeType.ADDED {@link org.apache.sling.api.resource.observation.ResourceChange.ChangeType}
   * Goes recursively through the resource tree and ensures the properties from 'per:PageContent'
   * are not null and valid values (E.g. canonicalLink value is unique per resource)
   *
   * @param page page resource the ResourceChange triggered on
   */
  private void handlePages(Resource page) {
    StringBuilder query = new StringBuilder();
    query.append("[" + JCR_PRIMARY_TYPE + "] is '" + PAGE_PRIMARY_TYPE + "'")
        .append(" and ")
        .append("[" + JCR_CONTENT + "/" + JCR_PRIMARY_TYPE + "] is '" + PAGE_CONTENT_TYPE + "'");

    List<Resource> children = new ResourceStream(page)
        .stream(r -> true)
        .filter(resourceFilter.parse(query.toString()))
        .collect(Collectors.toList());
    children.forEach(res -> {
      log.info(res.getPath());
      try {
        handleProperties(res, true);
      } catch (PersistenceException e) {
        log.error("Error: '{}', At Resource Path: '{}', Property Name: '{}'",
            e.getMessage(), e.getResourcePath(), e.getPropertyName());
      }
      ValueMap properties = PerUtil.getProperties(res);
      for (Object key : properties.keySet()) {
        Object value = properties.get(key);
        log.info("Properties -->  { " + key + " : " + value + " }");
      }
    });
  }

  /**
   * Handler for ChangeType.CHANGED {@link org.apache.sling.api.resource.observation.ResourceChange.ChangeType}
   *
   * @param resource Resource to look for the properties (Either per:Page or per:PageContent)
   * @param goToJcrContent If true then if the given resource is not the JCR Content it will look
   * that one up
   */
  private static void handleProperties(Resource resource, boolean goToJcrContent)
      throws PersistenceException {
    ModifiableValueMap props = PerUtil.getModifiableProperties(resource, goToJcrContent);

    if (!props.containsKey(PROTOCOL) || props.get(PROTOCOL) == null || ""
        .equals(props.get(PROTOCOL).toString())) {
      props.put(PROTOCOL, DEFAULT_PROTOCOL);
    }

    if (!props.containsKey(HOSTNAME) || props.get(HOSTNAME) == null || ""
        .equals(props.get(HOSTNAME).toString())) {
      props.put(HOSTNAME, DEFAULT_HOSTNAME);
    }

    if (goToJcrContent) {
      props.put(CANONICAL_LINK_ELEMENT, resource.getPath());
    } else {
      props
          .put(CANONICAL_LINK_ELEMENT, PerUtil.findParentAs(resource, PAGE_PRIMARY_TYPE).getPath());
    }

    if (!props.containsKey(EXCLUDE_FROM_NAVIGATION) || props.get(EXCLUDE_FROM_NAVIGATION) == null) {
      props.put(EXCLUDE_FROM_NAVIGATION, false);
    }

    if (goToJcrContent) {
      resource.getChild(JCR_CONTENT).getResourceResolver().commit();
    } else {
      resource.getResourceResolver().commit();
    }
  }
}