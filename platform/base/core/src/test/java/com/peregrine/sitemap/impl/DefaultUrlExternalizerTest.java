package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.PrefixAndCutUrlExternalizerBaseTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultUrlExternalizerTest extends PrefixAndCutUrlExternalizerBaseTestBase<DefaultUrlExternalizer> {

    public DefaultUrlExternalizerTest() {
        super(new DefaultUrlExternalizer());
    }

    @Test
    public void map() {
        mapAndCompare("", "");
        basicTest();
        example.setDomains();
        mapAndCompare(_CONTENT_EXAMPLE_PAGES + ".html", _CONTENT_EXAMPLE_PAGES + ".html");
    }

}
