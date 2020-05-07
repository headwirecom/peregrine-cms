package com.peregrine.sitemap;

import com.peregrine.sitemap.impl.SiteStructureTestBase;

import static junitx.framework.Assert.assertEquals;

public class PrefixAndCutUrlExternalizerBaseTestBase<E extends PrefixAndCutUrlExternalizerBase>
        extends SiteStructureTestBase {

    protected static final int CUT_COUNT = 3;

    protected final E model;

    public PrefixAndCutUrlExternalizerBaseTestBase(final E model) {
        this.model = model;
    }

    protected void mapAndCompare(final String expected, final String input) {
        assertEquals(expected, model.map(resourceResolver, input));
    }

    protected void fullTest() {
        mapAndCompare("http://www.example.com", "");
        basicTest();
    }

    protected void basicTest() {
        mapAndCompare("http://www.example.com", _CONTENT_EXAMPLE_PAGES + ".html");
        mapAndCompare("http://www.example.com/parent.html", _CONTENT_EXAMPLE_PAGES_ + "parent.html");
        mapAndCompare("http://www.example.com/parent/page.html", PAGE_PATH + ".html");
        mapAndCompare("http://www.example.com", _CONTENT_EXAMPLE_PAGES + ".x.html");
        mapAndCompare("http://www.example.com", _CONTENT_EXAMPLE_PAGES + ".sitemap.html");
        mapAndCompare("http://www.example.com/sitemap.xml", _CONTENT_EXAMPLE_PAGES + ".sitemap.xml");
        mapAndCompare("http://www.example.com/sitemap.1.xml", _CONTENT_EXAMPLE_PAGES + ".sitemap.1.xml");
    }

}
