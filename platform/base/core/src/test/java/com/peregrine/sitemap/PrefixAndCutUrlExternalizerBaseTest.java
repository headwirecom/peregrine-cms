package com.peregrine.sitemap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junitx.framework.Assert.assertEquals;
import org.apache.sling.api.resource.ResourceResolver;

@RunWith(MockitoJUnitRunner.class)
public final class PrefixAndCutUrlExternalizerBaseTest {

    private final PrefixAndCutUrlExternalizerBase model = new PrefixAndCutUrlExternalizerBase() {
    	
    	{
    		setCutCount(3);
    	}

		@Override
		protected CharSequence getPrefix(final ResourceResolver resourceResolver, final String url) {
			return prefix;
		}
    	
    };
    
    private String prefix = "http://www.example.com";

    private void mapAndCompare(final String expected, final String input) {
        assertEquals(expected, model.map(null, input));
    }

    @Test
    public void map() {
        mapAndCompare("http://www.example.com", "");
        mapAndCompare("http://www.example.com", "/content/example/pages.html");
        mapAndCompare("http://www.example.com/page.html", "/content/example/pages/page.html");
        mapAndCompare("http://www.example.com/page/sub.html", "/content/example/pages/page/sub.html");
        mapAndCompare("http://www.example.com", "/content/example/pages.x.html");
        mapAndCompare("http://www.example.com", "/content/example/pages.sitemap.html");
        mapAndCompare("http://www.example.com/sitemap.xml", "/content/example/pages.sitemap.xml");
        mapAndCompare("http://www.example.com/sitemap.1.xml", "/content/example/pages.sitemap.1.xml");
        prefix = null;
        mapAndCompare("/content/example/pages.sitemap.1.xml", "/content/example/pages.sitemap.1.xml");
    }

}
