package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public final class CacheBuilderBaseTest extends SlingResourcesTest {

    private final CacheBuilderBase model = Mockito.spy(new CacheBuilderBase() {

        {
            setLocation("/var/cache");
        }

        @Override
        protected ResourceResolver getServiceResourceResolver() {
            return resourceResolver;
        }

        @Override
        protected Resource buildCache(final Resource rootPage, final Resource cache) {
            return cache;
        }

        @Override
        protected void rebuildImpl(final String rootPagePath) { }

    });

    @SuppressWarnings("unchecked")
	@Test
    public void rebuilds_throwLoginException() throws LoginException, PersistenceException {
        when(model.getServiceResourceResolver()).thenThrow(LoginException.class);
        model.rebuild(StringUtils.EMPTY);
        verify(resourceResolver, times(0)).commit();
        model.rebuildAll();
        verify(resourceResolver, times(0)).commit();
    }

    @Test
    public void rebuilds_throwPersistenceException() throws LoginException, PersistenceException {
        doThrow(PersistenceException.class).when(resourceResolver).commit();
        model.rebuild(StringUtils.EMPTY);
        verify(resourceResolver, times(1)).commit();
        model.rebuildAll();
        verify(resourceResolver, times(2)).commit();
    }

}