package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import org.apache.jackrabbit.JcrConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public final class PageRecognizerBaseTest extends SlingResourcesTest {

    private static final String PRIMARY_TYPE = "test:Page";
    private static final String CONTENT_TYPE = "test:PageContent";
    private static final String EXCLUDE_SITE_MAP_PROPERTY = "exclude";

    private final PageRecognizerBase model = new PageRecognizerBase(PRIMARY_TYPE, CONTENT_TYPE, EXCLUDE_SITE_MAP_PROPERTY) {
        @Override
        protected boolean isPageImpl(final Page candidate) {
            return true;
        }
    };
    private final Page candidate = new Page(page);

    @Test
    public void isPage() {
        assertFalse(model.isPage(candidate));
        parent.putProperty(JCR_PRIMARY_TYPE, PRIMARY_TYPE);
        assertFalse(model.isPage(new Page(parent)));
        page.putProperty(JCR_PRIMARY_TYPE, PRIMARY_TYPE);
        assertFalse(model.isPage(candidate));
        content.putProperty(JCR_PRIMARY_TYPE, CONTENT_TYPE);
        assertFalse(model.isPage(candidate));
        page.putProperty(SLING_RESOURCE_TYPE, RESOURCE_TYPE);
        assertTrue(model.isPage(candidate));
        page.putProperty(EXCLUDE_SITE_MAP_PROPERTY, true);
        assertFalse(model.isPage(candidate));
    }

}