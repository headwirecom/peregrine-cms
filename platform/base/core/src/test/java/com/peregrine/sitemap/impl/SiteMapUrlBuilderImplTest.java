package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.SiteMapConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapUrlBuilderImplTest extends SlingResourcesTest {

    private final SiteMapUrlBuilderImpl model = new SiteMapUrlBuilderImpl();

    @Test
    public void buildSiteMapUrl() {
        assertEquals("/content/parent/page.sitemap.xml", model.buildSiteMapUrl(page, 0));
        assertEquals("/content/parent/page.sitemap.1.xml", model.buildSiteMapUrl(page, 1));
    }

    @Test
    public void getIndex() {
        request.setExtension(SiteMapConstants.XML);
        request.setSelectors((String[])null);
        assertEquals(0, model.getIndex(request));
        request.setSelectorsString("sitemap");
        assertEquals(0, model.getIndex(request));
        request.setSelectorsString("sitemap.0");
        assertEquals(0, model.getIndex(request));
        request.setSelectorsString("sitemap.1");
        assertEquals(1, model.getIndex(request));
        request.setSelectorsString("sitemap.x");
        assertEquals(-1, model.getIndex(request));
        request.setSelectorsString("sitemap..x");
        assertEquals(0, model.getIndex(request));
    }

}
