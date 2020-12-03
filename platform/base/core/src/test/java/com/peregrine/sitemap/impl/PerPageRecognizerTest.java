package com.peregrine.sitemap.impl;

import com.peregrine.commons.util.PerConstants;
import com.peregrine.sitemap.PageRecognizerBaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PerPageRecognizerTest extends PageRecognizerBaseTest {

    public PerPageRecognizerTest() {
        super(new PerPageRecognizer());
        jcrContent.putProperty(PerConstants.PER_REPLICATION_REF, "some-ref");
    }

    @Test
    public void isPage() {
        super.isPage();
    }

}