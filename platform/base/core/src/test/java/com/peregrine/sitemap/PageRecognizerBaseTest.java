package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.peregrine.commons.util.PerConstants.EXCLUDE_TREE_FROM_SITEMAP;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PageRecognizerBaseTest extends SlingResourcesTest {

    private static final String PRIMARY_TYPE = "test:Page";
    private static final String CONTENT_TYPE = "test:PageContent";
    private static final String EXCLUDE_SITE_MAP_PROPERTY = "exclude";

    protected final Page candidate = new Page(page);
    protected final PageRecognizerBase model;

    protected PageRecognizerBaseTest(final PageRecognizerBase model) {
        this.model = model;
        page.removeProperty(SLING_RESOURCE_TYPE);
        page.getContent().removeProperty(SLING_RESOURCE_TYPE);
    }

    public PageRecognizerBaseTest() {
        this(new PageRecognizerBase(PRIMARY_TYPE, CONTENT_TYPE, EXCLUDE_SITE_MAP_PROPERTY, EXCLUDE_TREE_FROM_SITEMAP) {
            @Override
            protected boolean isPageImpl(final Page candidate) {
                return true;
            }
        });
    }

    @Test
    public void isPage() {
        assertFalse(model.isPage(candidate));
        parent.setPrimaryType(model.getPagePrimaryType());
        assertFalse(model.isPage(new Page(parent)));
        page.setPrimaryType(model.getPagePrimaryType());
        assertFalse(model.isPage(candidate));
        jcrContent.setPrimaryType(model.getPageContentPrimaryType());
        assertFalse(model.isPage(candidate));
        page.setResourceType(RESOURCE_TYPE);
        assertTrue(model.isPage(candidate));
        page.putProperty(model.getExcludeFromSiteMapPropertyName(), true);
        assertFalse(model.isPage(candidate));
    }

}