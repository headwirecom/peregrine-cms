package com.peregrine.sitemap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class SiteMapEntry {

    private final Map<String, String> properties = new LinkedHashMap<>();
    private final Page page;
    private String url;

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
        if (isBlank(name)) {
            return null;
        }

        if (isBlank(value)) {
            return properties.remove(name);
        }

        return properties.put(name, value);
    }

    public Set<Map.Entry<String, String>> getProperties() {
        return properties.entrySet();
    }
}
