package com.peregrine.sitemap;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SiteMapEntry {

    private final Page page;
    private String url;
    private final Map<String, String> properties = new LinkedHashMap<>();

    public SiteMapEntry(final Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String putProperty(final String name, final String value) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        if (StringUtils.isBlank(value)) {
            return properties.remove(name);
        }

        return properties.put(name, value);
    }

    public Set<Map.Entry<String, String>> getProperties() {
        return properties.entrySet();
    }
}
