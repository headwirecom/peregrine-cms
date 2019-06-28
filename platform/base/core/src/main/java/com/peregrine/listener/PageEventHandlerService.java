package com.peregrine.listener;

import static com.peregrine.commons.util.PerConstants.CANONICAL_LINK_ELEMENT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PROPERTIES;
import static com.peregrine.commons.util.PerConstants.RESOURCE_CHANGE_LISTENER;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.CHANGES;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

import com.peregrine.commons.util.PerUtil;
import com.peregrine.seo.UrlExternalizer;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChange.ChangeType;
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
        PATHS + EQUALS + "glob:" + SITES_ROOT + "/**",
        CHANGES + EQUALS + "ADDED",
        CHANGES + EQUALS + "CHANGED",
        CHANGES + EQUALS + "REMOVED"
    }
)
public class PageEventHandlerService implements ResourceChangeListener {

  private static final Logger log = LoggerFactory.getLogger(PageEventHandlerService.class);

  @Reference
  @SuppressWarnings("unused")
  private ResourceResolverFactory factory;

  @Reference
  private ResourcePredicates resourceFilter;

  @Reference
  private UrlExternalizer externalizer;

  @Override
  public void onChange(final List<ResourceChange> changes) {
    if (changes != null) {
      for (ResourceChange change : changes) {
        log.trace("Resource Change: '{}'", change);

        try (ResourceResolver resolver = PerUtil.loginService(factory, RESOURCE_CHANGE_LISTENER)) {
          Resource resource = PerUtil.getResource(resolver, change.getPath());
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
              handleProperties(resource, PAGE_PRIMARY_TYPE.equals(primaryType), ChangeType.CHANGED);
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
    new ResourceStream(page)
        .stream(r -> true)
        .filter(resourceFilter.parse("[" + JCR_PRIMARY_TYPE + "] is '" + PAGE_PRIMARY_TYPE + "'"))
        .forEach(res -> {
          log.info(res.getPath());
          handleProperties(res, true, ChangeType.ADDED);
        });
  }

  /**
   * Handler for ChangeType.CHANGED {@link org.apache.sling.api.resource.observation.ResourceChange.ChangeType}
   *
   * @param resource Resource to look for the properties (Either per:Page or per:PageContent)
   * @param goToJcrContent If true then if the given resource is not the JCR Content it will look
   * @param changeType type the resource change is triggered on that one up
   */
  private void handleProperties(Resource resource, boolean goToJcrContent, ChangeType changeType) {
    try {
      ModifiableValueMap props = PerUtil.getModifiableProperties(resource, goToJcrContent);

      Consumer<? super Pair<String, ?>> canonical = dict -> props.put(dict.getKey(),
          externalizer.buildExternalizedLink(
              goToJcrContent ? resource.getResourceResolver()
                  : resource.getParent().getResourceResolver(),
              goToJcrContent ? resource.getPath() : resource.getParent().getPath())
              + ".html");

      PAGE_PROPERTIES.forEach(pair -> {
        if (!props.containsKey(pair.getLeft()) || props.get(pair.getKey()).toString().isEmpty()) {
          props.put(pair.getKey(), pair.getValue());
          if (pair.getLeft().equals(CANONICAL_LINK_ELEMENT)) {
            canonical.accept(pair);
          }
        }
        if (pair.getLeft().equals(CANONICAL_LINK_ELEMENT) && changeType.equals(ChangeType.ADDED)) {
          canonical.accept(pair);
        }
      });

      if (goToJcrContent) {
        resource.getChild(JCR_CONTENT).getResourceResolver().commit();
      } else {
        resource.getResourceResolver().commit();
      }
    } catch (PersistenceException e) {
      log.error("Error: '{}', At Resource Path: '{}', Property Name: '{}' --->>> Cause: '{}'",
          e.getMessage(), e.getResourcePath(), e.getPropertyName(), e.getCause());
    }
  }
}