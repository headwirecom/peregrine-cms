package com.peregrine.sitemap.impl;

import com.peregrine.SlingServletTest;
import com.peregrine.sitemap.SiteMapFilesCache;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import junitx.util.PrivateAccessor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapServletTest extends SlingServletTest {

    private static final String RESULT = "<xml />";

    private final SiteMapServlet model = new SiteMapServlet();

    @Mock
    private SiteMapUrlBuilder urlBuilder;

    @Mock
    private SiteMapFilesCache cache;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "urlBuilder", urlBuilder);
        PrivateAccessor.setField(model, "cache", cache);
    }

    @Test
    public void incorrectIndex() throws IOException {
        when(urlBuilder.getIndex(request)).thenReturn(-1);
        model.doGet(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, status);
        assertTrue(StringUtils.isBlank(writer.toString()));
    }

    @Test
    public void doGet() throws IOException {
        when(urlBuilder.getIndex(request)).thenReturn(0);
        when(cache.get(resource, 0)).thenReturn(RESULT);
        model.doGet(request, response);
        assertNotEquals(HttpServletResponse.SC_NOT_FOUND, status);
        assertEquals(RESULT, writer.toString());
    }

}
