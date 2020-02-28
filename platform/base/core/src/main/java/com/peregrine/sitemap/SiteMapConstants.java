package com.peregrine.sitemap;

import com.peregrine.commons.Strings;

public final class SiteMapConstants {

    public static final String TXT = "txt";
    public static final String XML = "xml";
    public static final String HTML = "html";
    public static final String DOT_HTML = Strings.DOT + HTML;

    public static final String SITE_MAP_INDEX = "sitemapindex";
    public static final String SITE_MAP = "sitemap";

    public static final String URL_SET = "urlset";

    public static final String URL = "url";
    public static final String LOC = "loc";
    public static final String LAST_MOD = "lastmod";
    public static final String CHANGE_FREQ = "changefreq";
    public static final String PRIORITY = "priority";

    public static final String WEEKLY = "weekly";
    public static final String DEFAULT_PRIORITY = "0.5";

    private SiteMapConstants() {
        throw new UnsupportedOperationException();
    }

}
