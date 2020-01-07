package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PageRecognizersAndChainTest extends SlingResourcesTest {

    private Page candidate = new Page(page);

    @Mock
    private PageRecognizer recognizer1;
    @Mock
    private PageRecognizer recognizer2;

    private PageRecognizersAndChain model;

    @Before
    public void setUp() {
        model = new PageRecognizersAndChain(recognizer1, recognizer2);
    }

    @Test
    public void isPage() {
        when(recognizer1.isPage(candidate)).thenReturn(false);
        when(recognizer2.isPage(candidate)).thenReturn(false);
        assertFalse(model.isPage(candidate));

        when(recognizer1.isPage(candidate)).thenReturn(true);
        assertFalse(model.isPage(candidate));

        when(recognizer2.isPage(candidate)).thenReturn(true);
        assertTrue(model.isPage(candidate));
    }

}