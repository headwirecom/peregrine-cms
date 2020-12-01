package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public final class VersioningResourceResolverFactoryImplTest extends SlingResourcesTest {

    private final VersioningResourceResolverFactoryImpl model = new VersioningResourceResolverFactoryImpl();

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "resourceResolverFactory", repo.getResolverFactory());
    }

    @Test
    public void createResourceResolver() throws LoginException {
        assertNotNull(model.createResourceResolver());
    }

}