package com.peregrine.admin.resource;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.spi.resource.provider.ProviderContext;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.COMPONENTS;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static org.apache.sling.api.resource.Resource.RESOURCE_TYPE_NON_EXISTING;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

public class TenantAppsResourceProviderService
    extends ResourceProvider
    implements TenantAppsResourceProvider
{
    private static List<String> IGNORED_ATTRIBUTES = new ArrayList<>(Arrays.asList("jcr:created"));

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @SuppressWarnings("rawtypes")
    private volatile ServiceRegistration serviceRegistration;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean active;
    private String themeName;
    private String tenantName;
    private String sourceRootPath;
    private String sourceComponentsPath;
    private String targetRootPath;
    private String targetComponentsPath;

    //---------- Service Registration

    long registerService(Bundle bundle, String tenantName, String themeName) {
        this.tenantName = tenantName;
        this.themeName = themeName;
        log.info("Theme Name: '{}', Tenant Name: '{}'", themeName, tenantName);
        sourceRootPath = APPS_ROOT + "/" + themeName;
        sourceComponentsPath = sourceRootPath + "/" + COMPONENTS;
        targetRootPath = APPS_ROOT + "/" + tenantName;
        targetComponentsPath = targetRootPath + "/" + COMPONENTS;

        final Dictionary<String, Object> props = new Hashtable<>();
        props.put("label", "Tenant Apps Resource Provider for " + tenantName);
        props.put(SERVICE_DESCRIPTION, "Provides the /apps/" + tenantName + " 'link' resources as synthetic resources");
        props.put(SERVICE_VENDOR, "The Apache Software Foundation");
        props.put(ResourceProvider.PROPERTY_ROOT, APPS_ROOT + "/" + tenantName);
        props.put(THEME_NAME, themeName);
        props.put(TENANT_NAME, tenantName);
        props.put(getClass().getName(), bundle.getBundleId());

        log.info("Before Register RARPS with props: '{}'", props);
        serviceRegistration = bundle.getBundleContext().registerService(
            new String[] {ResourceProvider.class.getName(), TenantAppsResourceProvider.class.getName()}, this, props
        );
        log.info("After Register RARPS, service registration: '{}'", serviceRegistration);
        active = true;
        return (Long) serviceRegistration.getReference().getProperty(Constants.SERVICE_ID);
    }

    void unregisterService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
            } catch ( final IllegalStateException ise ) {
                // this might happen on shutdown, so ignore
            }
            serviceRegistration = null;
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getTenantName() {
        return tenantName;
    }

    @Override
    public String getThemeName() {
        return themeName;
    }

    private Resource createSyntheticFromResource(ResourceResolver resourceResolver, Resource source) {
        ValueMap properties = source.getValueMap();
        ResourceMetadata metadata = new ResourceMetadataWrapper();
        String oldPath = source.getPath();
        String newPath = oldPath.replace(themeName, tenantName);
        metadata.setResolutionPath(newPath);
        Map<String,String> parameters = new HashMap<>();
        boolean isComponent = oldPath.contains("/" + COMPONENTS + "/");
        String resourceSuperType = null;
        if(isComponent) {
            resourceSuperType = themeName + "/" + COMPONENTS + "/" + source.getName();
        }
        parameters.put(SLING_RESOURCE_SUPER_TYPE, resourceSuperType);
        for(Entry<String, Object> entry: properties.entrySet()) {
            if(!IGNORED_ATTRIBUTES.contains(entry.getKey())) {
                parameters.put(entry.getKey(), entry.getValue() + "");
            }
        }
        metadata.setParameterMap(parameters);
        metadata.setResolutionPath(newPath);
        metadata.setResolutionPathInfo(newPath);
        metadata.setCreationTime(System.currentTimeMillis());
        return new SuperSyntheticResource(
            resourceResolver,
            metadata,
            source.getResourceType(),
            resourceSuperType
        );
    }

    @Override
    public Resource getResource(ResolveContext ctx, String path, ResourceContext resourceContext, Resource parent) {
        log.info("Get Resource, path: '{}', parent: '{}'", path, parent);
        log.info("Get Resource, resolve context: '{}', resource context: '{}'", ctx, resourceContext);
        String resourcePath;
        if(path.startsWith("/")) {
            resourcePath = path;
        } else {
            resourcePath = parent.getPath() + "/" + path;
        }
        Resource answer = null;
        ResourceResolver resourceResolver = ctx.getResourceResolver();
        log.info("Resource Path: '{}', targetRootPath: '{}'", resourcePath, targetRootPath);
        if (resourcePath.equals(targetRootPath)) {
            Resource source = resourceResolver.getResource(sourceRootPath);
            log.info("Source from path: '{}': '{}'", sourceRootPath, source);
            if (source != null && !source.isResourceType(RESOURCE_TYPE_NON_EXISTING)) {
                answer = createSyntheticFromResource(resourceResolver, source);
            }
        } else if (resourcePath.equals(targetComponentsPath)) {
            Resource source = resourceResolver.getResource(sourceComponentsPath);
            if (source != null && !source.isResourceType(RESOURCE_TYPE_NON_EXISTING)) {
                answer = createSyntheticFromResource(resourceResolver, source);
            }
        } else if (
            resourcePath.startsWith(targetComponentsPath + "/") &&
            resourcePath.indexOf('/', targetComponentsPath.length() + 1) < 0
        ) {
            String sourcePath = resourcePath.replace(tenantName, themeName);
            log.info("Theme Path: '{}' from Tenant Path: '{}'", sourcePath, resourcePath);
            Resource source = resourceResolver.getResource(sourcePath);
            if (source != null && !source.isResourceType(RESOURCE_TYPE_NON_EXISTING)) {
                answer = createSyntheticFromResource(resourceResolver, source);
            }
        }
        log.info("Return resource: '{}'", answer);
        return answer;
    }

    @Override
    public Iterator<Resource> listChildren(ResolveContext ctx, Resource parent) {
        List<Resource> answer = null;
        log.info("List Children, resolve-context: '{}', parent: '{}'", ctx, parent);
        String resourcePath = parent.getPath();
        ResourceResolver resourceResolver = ctx.getResourceResolver();
        if (resourcePath.equals(targetRootPath)) {
            Resource source = resourceResolver.getResource(sourceComponentsPath);
            if (source != null && !source.isResourceType(RESOURCE_TYPE_NON_EXISTING)) {
                answer = new ArrayList<>();
                answer.add(createSyntheticFromResource(resourceResolver, source));
            }
        } else if (resourcePath.equals(targetComponentsPath)) {
            Resource source = resourceResolver.getResource(sourceComponentsPath);
            if (source != null && !source.isResourceType(RESOURCE_TYPE_NON_EXISTING)) {
                answer = new ArrayList<>();
                Iterator<Resource> i = source.listChildren();
                while (i.hasNext()) {
                    Resource item = i.next();
                    answer.add(createSyntheticFromResource(resourceResolver, item));
                }
            }
        }
        return answer != null ? answer.iterator() : null;
    }

    @Override
    public void start(ProviderContext ctx) {
        log.info("Provider Start, context: '{}'", ctx);
        super.start(ctx);
    }

    @Override
    public void stop() {
        log.info("Provider Stop");
        super.stop();
    }

    public class ResourceMetadataWrapper extends ResourceMetadata {

        private boolean set = false;

        @Override
        public void setParameterMap(Map<String, String> parameterMap) {
            // Allow the map to be set only once as Sling tries to reset the map
            // with wrong values
            //AS TODO: Investigate why
            if(!set) {
                super.setParameterMap(parameterMap);
            }
            set = true;
        }

        @Override
        public Map<String, String> getParameterMap() {
            Map<String, String> answer = super.getParameterMap();
            log.trace("Get Parameter Map: '{}'", answer);
            return answer;
        }

        @Override
        public void lock() {
            // Disable Locking as this leads to error
            //AS TODO: investigate why this is called and later Sling tries to change it
//            log.info("Resource Meta Data locked", new Exception("Here we are"));
//            super.lock();
        }
    }
}
