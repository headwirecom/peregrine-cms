package com.peregrine.sitemap;

import com.peregrine.sitemap.PrefixAndCutUrlExternalizerBaseTest.PrefixAndCutUrlExternalizerBaseImpl;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class PrefixAndCutUrlExternalizerBaseTest
        extends PrefixAndCutUrlExternalizerBaseTestBase<PrefixAndCutUrlExternalizerBaseImpl> {

    public PrefixAndCutUrlExternalizerBaseTest() {
        super(new PrefixAndCutUrlExternalizerBaseImpl());
    }

    @Test
    public void map() {
        fullTest();
        model.setPrefix(null);
        mapAndCompare("/content/example/pages.sitemap.1.xml", "/content/example/pages.sitemap.1.xml");
    }

    public static final class PrefixAndCutUrlExternalizerBaseImpl extends PrefixAndCutUrlExternalizerBase {

        private String prefix = EXAMPLE_COM;

        {
            setCutCount(CUT_COUNT);
        }

        @Override
        protected CharSequence getPrefix(final ResourceResolver resourceResolver, final String url) {
            return prefix;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }
    }

}
