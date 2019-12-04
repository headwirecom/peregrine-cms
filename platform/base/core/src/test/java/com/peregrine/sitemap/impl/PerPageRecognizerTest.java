package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.PageRecognizerBaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PerPageRecognizerTest extends PageRecognizerBaseTest {

    public PerPageRecognizerTest() {
        super(new PerPageRecognizer());
    }

    @Test
    public void isPage() {
        super.isPage();
    }

}