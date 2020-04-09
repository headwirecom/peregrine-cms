package com.peregrine.admin.resource;

public interface TenantAppsResourceProvider {
    String TENANT_NAME = "tenant.name";
    String THEME_NAME = "theme.name";

    boolean isActive();
    String getTenantName();
    String getThemeName();
}
