package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class HasNameTest extends SlingResourcesTest {

    private final HasName model = new HasName() {};

    @Test
    public void getName() {
        assertEquals(model.getClass().getName(), model.getName());
    }

}