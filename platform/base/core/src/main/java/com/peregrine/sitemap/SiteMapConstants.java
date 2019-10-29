package com.peregrine.sitemap;

import org.apache.sling.api.resource.ResourceResolverFactory;

import java.util.HashMap;
import java.util.Map;

public class SiteMapConstants {

    public static final String SITE_MAPS_SUB_SERVICE = "sitemaps";

    public static Map<String, Object> getAuthenticationInfoMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put(ResourceResolverFactory.SUBSERVICE, SiteMapConstants.SITE_MAPS_SUB_SERVICE);
        return map;
    }

}
