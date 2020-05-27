package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class UrlExternalizerBaseTest extends SlingResourcesTest {

    private static final String PREFIX = "http://www.example.com";

    private final UrlExternalizerBase model = new UrlExternalizerBase() {
        @Override
        public String map(final ResourceResolver resourceResolver, final String url) {
            return PREFIX + url;
        }
    };

    @Test
    public void map() {
        assertEquals("http://www.example.com/content/parent.html", model.map(parent));
    }

}