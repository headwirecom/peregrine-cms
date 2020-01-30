package com.peregrine.sitemap.impl;

import com.peregrine.SlingServletTest;
import com.peregrine.sitemap.SiteMapEntry;
import com.peregrine.sitemap.SiteMapStructureCache;
import junitx.util.PrivateAccessor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ApacheRewriteMapServletTest extends SlingServletTest {

    private final ApacheRewriteMapServlet model = new ApacheRewriteMapServlet();

    @Mock
    private SiteMapStructureCache structure;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "structure", structure);
    }

    @Test
    public void noEntries() throws IOException {
        when(structure.get(resource)).thenReturn(null);
        model.doGet(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, status);
        assertTrue(StringUtils.isBlank(writer.toString()));
    }

    @Test
    public void doGet() throws IOException {
        final List<SiteMapEntry> entries = new LinkedList<>();
        SiteMapEntry entry = new SiteMapEntry("/content/page-1");
        entry.setUrl("http://www.example.com/page-1.html");
        entries.add(entry);
        entry = new SiteMapEntry("/content/page-2");
        entry.setUrl("/page-2.html");
        entries.add(entry);
        when(structure.get(resource)).thenReturn(entries);
        model.doGet(request, response);
        assertNotEquals(HttpServletResponse.SC_NOT_FOUND, status);
        assertEquals(
                StringUtils.strip("/page-1.html /content/page-1.html\n" + "/page-2.html /content/page-2.html"),
                StringUtils.strip(writer.toString())
        );
    }

}
