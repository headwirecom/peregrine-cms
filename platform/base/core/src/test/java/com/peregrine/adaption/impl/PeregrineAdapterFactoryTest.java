package com.peregrine.adaption.impl;

import com.peregrine.adaption.PerPage;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;

import static com.peregrine.adaption.impl.PerTestUtil.createPageResource;
import static com.peregrine.adaption.impl.PerTestUtil.createResource;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Andreas Schaefer on 6/20/17.
 */
public class PeregrineAdapterFactoryTest {

    @Test
    public void adaptPageFromPageResource() throws Exception {
        Resource pageResource = createPageResource(null, "resource-1");
        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(pageResource, PerPage.class);
        assertNotNull("Page could not be adapted", page);
        checkPage("Adapted Page Resource is not what was excepted", pageResource, page);
    }

    private void checkPage(String message, Resource expected, PerPage found) {
        assertEquals(message, expected, found.getResource());
    }

    private void checkPage(String message, PerPage expected, PerPage found) {
        assertEquals(message, expected.getResource(), found.getResource());
    }

    @Test
    public void adaptPageFromContentTest() throws Exception {
        Resource pageResource = createPageResource(null, "resource-2");
        Resource jcrContent = createResource(pageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(jcrContent, PerPage.class);
        assertNotNull("Page Content could not be adapted", page);
        checkPage("Adapted Page Resource is not what was excepted", pageResource, page);
    }

    @Test
    public void adaptPageFromContentChildTest() throws Exception {
        Resource pageResource = createPageResource(null, "resource-3");
        Resource jcrContent = createResource(pageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);
        Resource jcrContentChild = createResource(jcrContent, "My Gugus", "gugus");

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(jcrContentChild, PerPage.class);
        assertNotNull("Page Content could not be adapted", page);
        checkPage("Adapted Page Resource is not what was excepted", pageResource, page);
    }

    @Test
    public void noAdaptNoPageContentChild() throws Exception {
        Resource pageResource = createPageResource(null, "resource-4");
        Resource pageNoContentResource = createResource(pageResource, "My Gugus", "gugus");

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(pageNoContentResource, PerPage.class);
        assertNull("Page Child should not be adapted as it is a no-content child", page);
    }

    @Test
    public void noAdaptNoPageChild() throws Exception {
        Resource notPageResource = createResource(null, "resource-5", "super:gugus");
        Resource jcrContent = createResource(notPageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(notPageResource, PerPage.class);
        assertNull("No Page should not be adapted as it has no Page Type", page);
        page = factory.getAdapter(jcrContent, PerPage.class);
        assertNull("No Page Content should not be adapted as it has no Page parent", page);
    }
}