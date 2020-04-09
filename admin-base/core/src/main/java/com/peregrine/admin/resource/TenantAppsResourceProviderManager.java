package com.peregrine.admin.resource;

import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface TenantAppsResourceProviderManager {
    void registerTenant(String tenantName, String themeName);
    void registerTenant(Resource tenantRoot);
    void unregisterTenant(String tenantName);
    void unregisterTenant(Resource tenantRoot);
    /** @return List of all the registered tenants **/
    List<String> getListOfTenants();
}
