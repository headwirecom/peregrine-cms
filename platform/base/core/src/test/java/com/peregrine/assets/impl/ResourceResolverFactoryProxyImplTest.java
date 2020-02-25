package com.peregrine.assets.impl;

import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ResourceResolverFactoryProxyImplTest {

	private final ResourceResolverFactoryProxyImpl model = new ResourceResolverFactoryProxyImpl();

	@Mock
	private ResourceResolverFactory resourceResolverFactory;

	@Mock
	private ResourceResolver resourceResolver;

	@Before
	public void setUp() throws NoSuchFieldException, LoginException {
		PrivateAccessor.setField(model, "resourceResolverFactory", resourceResolverFactory);
		when(resourceResolverFactory.getServiceResourceResolver(any())).thenReturn(resourceResolver);
	}

	@Test
	public synchronized void testBasic() throws LoginException {
		assertEquals(resourceResolver, model.getServiceResourceResolver());
	}
}
