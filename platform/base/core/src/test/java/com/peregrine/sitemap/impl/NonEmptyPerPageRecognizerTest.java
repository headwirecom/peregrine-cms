package com.peregrine.sitemap.impl;

import com.peregrine.commons.Page;
import com.peregrine.sitemap.PageRecognizerBaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public final class NonEmptyPerPageRecognizerTest extends PageRecognizerBaseTest {

    public NonEmptyPerPageRecognizerTest() {
        super(new NonEmptyPerPageRecognizer());
    }

    @Test
    public void isPage() {
        super.isPage();
        parent.putProperty(JCR_PRIMARY_TYPE, model.getPagePrimaryType());
        parent.putProperty(SLING_RESOURCE_TYPE, RESOURCE_TYPE);
        assertFalse(model.isPage(new Page(parent)));
    }

}