package com.peregrine.admin.resource;

import org.junit.Test;

import static com.peregrine.commons.util.PerConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PathPatternTests {

    @Test
    public void siteHomePagesAndAssetsTree() {
        final String siteHome = "/content/example/";
        final String sitePages = siteHome + "pages/index";
        final String siteAssets = siteHome  + "assets/asset";
        final String siteRecycing = siteHome  + "recyclebin/" + sitePages;

        assertTrue(siteHome.matches(SITE_HOME_PATTERN));
        assertTrue(sitePages.matches(SITE_PAGES_PATTERN));
        assertTrue(siteAssets.matches(SITE_ASSETS_PATTERN));
        assertTrue(siteRecycing.matches(SITE_RECYCLEBIN_PATTERN));
        assertEquals("/content/example",
                sitePages.substring(0,
                    sitePages.indexOf(sitePages.replaceFirst(SITE_HOME_PATTERN, ""))-1));

    }
}
