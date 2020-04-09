package com.peregrine.admin.resource;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.resource.AdminResourceHandlerService.SOURCE_SITE;
import static com.peregrine.commons.util.PerConstants.ADMIN_SUB_SERVICE;
import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SITE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.loginService;

/**
 * This class will register all available Tenant Resource
 * Types on startup as well as when a new Tenant is created
 */
@Component(
    service=TenantAppsResourceProviderManager.class,
    immediate = true
)
public class TenantAppsResourceProviderManagerService
    implements TenantAppsResourceProviderManager
{
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, TenantAppsResourceProviderService> registeredServices = new HashMap<>();
    private BundleContext bundleContext;

    @Activate
    private void activate(BundleContext bundleContext) {
        log.info("Activate Started, bundle context: '{}'", bundleContext);
        this.bundleContext = bundleContext;
        try (ResourceResolver resourceResolver = loginService(resourceResolverFactory, ADMIN_SUB_SERVICE)) {
            Resource content = resourceResolver.getResource(CONTENT_ROOT);
            log.info("Content Resource: '{}'", content);
            // Look for all the Tenants in /content
            if(content != null) {
                Iterator<Resource> i = content.listChildren();
                while(i.hasNext()) {
                    Resource contentChild = i.next();
                    log.info("Content Child Resource: '{}'", contentChild);
                    ValueMap childProperties = contentChild.getValueMap();
                    String source = childProperties.get(SOURCE_SITE, String.class);
                    String primaryType = childProperties.get(JCR_PRIMARY_TYPE, String.class);
                    String tenantName = contentChild.getName();
                    log.info("Content Child Source: '{}', Primary Type: '{}'", source, primaryType);
                    if(source != null && !source.isEmpty() && primaryType.equals(SITE_PRIMARY_TYPE)) {
                        TenantAppsResourceProviderService service = new TenantAppsResourceProviderService();
                        log.info("Before Registering Tenant RP, tenant: '{}', theme: '{}'", tenantName, source);
                        long id = service.registerService(bundleContext.getBundle(), tenantName, source);
                        log.info("After Registering Tenant RP: service: '{}', id: '{}'", service, id);
                        registeredServices.put(tenantName, service);
                    }
                }
            }
        } catch (LoginException e) {
            log.error("Was not able to obtain Service Resource Resolver", e);
            e.printStackTrace();
        }
    }

    @Deactivate
    private void deactivate() {
        for(TenantAppsResourceProviderService service: registeredServices.values()) {
            log.info("Before UnRegistering Tenant RP, service: '{}'", service);
            service.unregisterService();
            log.info("After UnRegistering Tenant RP, service: '{}'", service);
        }
    }

    @Override
    public void registerTenant(String tenantName, String themeName) {
        if(isEmpty(tenantName)) {
            throw new IllegalArgumentException("Tenant Name must be specified");
        }
        if(isEmpty(themeName)) {
            throw new IllegalArgumentException("Theme Name must be specified");
        }
        TenantAppsResourceProviderService service = new TenantAppsResourceProviderService();
        service.registerService(bundleContext.getBundle(), tenantName, themeName);
        registeredServices.put(tenantName, service);
    }

    @Override
    public void registerTenant(Resource tenantRoot) {
        if(tenantRoot != null) {
            ValueMap childProperties = tenantRoot.getValueMap();
            String source = childProperties.get(SOURCE_SITE, String.class);
            String primaryType = childProperties.get(JCR_PRIMARY_TYPE, String.class);
            String tenantName = tenantRoot.getName();
            if(primaryType.equals(SITE_PRIMARY_TYPE)) {
                registerTenant(tenantName, source);
            } else {
                throw new IllegalArgumentException("Resource: '" + tenantRoot + "' is not a Peregrine Site");
            }
        }
    }

    @Override
    public void unregisterTenant(String tenantName) {
        TenantAppsResourceProviderService service = registeredServices.get(tenantName);
        if(service != null) {
            service.unregisterService();
        }
    }

    @Override
    public void unregisterTenant(Resource tenantRoot) {
        unregisterTenant(tenantRoot.getName());
    }

    @Override
    public List<String> getListOfTenants() {
        return new ArrayList<>(registeredServices.keySet());
    }
}
