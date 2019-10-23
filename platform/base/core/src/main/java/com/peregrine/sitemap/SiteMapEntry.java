package com.peregrine.sitemap;

public class SiteMapEntry {

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
}
