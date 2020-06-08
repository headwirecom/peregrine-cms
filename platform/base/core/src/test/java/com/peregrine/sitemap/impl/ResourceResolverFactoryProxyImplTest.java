package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class ResourceResolverFactoryProxyImplTest extends SlingResourcesTest {

    private final ResourceResolverFactoryProxyImpl model = new ResourceResolverFactoryProxyImpl();

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "resourceResolverFactory", repo.getResolverFactory());
    }

    @Test
    public void getServiceResourceResolver() throws LoginException {
        assertEquals(resourceResolver, model.getServiceResourceResolver());
    }

}