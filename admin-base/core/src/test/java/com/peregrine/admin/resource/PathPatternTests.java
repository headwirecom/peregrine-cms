package com.peregrine.admin.resource;

import org.junit.Test;

import static com.peregrine.commons.util.PerConstants.*;
import static org.junit.Assert.assertTrue;

public class PathPatternTests {

    @Test
    public void siteHomePagesAndAssetsTree() {
        final String siteHome = "/content/example/";
        final String sitePages = siteHome + "pages/index";
        final String siteAssets = siteHome  + "assets/asset";

        assertTrue(siteHome.matches(SITE_HOME_PATTERN));
        assertTrue(sitePages.matches(SITE_PAGES_PATTERN));
        assertTrue(siteAssets.matches(SITE_ASSETS_PATTERN));
    }
}
