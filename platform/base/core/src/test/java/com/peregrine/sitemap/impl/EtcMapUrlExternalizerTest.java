package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class EtcMapUrlExternalizerTest extends SlingResourcesTest {

    private final EtcMapUrlExternalizer model = new EtcMapUrlExternalizer();

    @Test
    public void map() {
        final String url = "/content/page.html";
        final String mappedUrl = "http://www.example.com/page.html";
        repo.map(url, mappedUrl);
        assertEquals(mappedUrl, model.map(resourceResolver, url));
    }

}