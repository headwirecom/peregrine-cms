package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.*;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.*;

import static com.peregrine.commons.util.PerConstants.DOMAINS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultSiteMapExtractorTest extends SiteStructureTestBase {

    private final DefaultSiteMapExtractor model = new DefaultSiteMapExtractor();
    private final PerPageRecognizer pageRecognizer = new PerPageRecognizer();
    private final DefaultUrlExternalizer urlExternalizer = new DefaultUrlExternalizer();
    private final LastModPropertyProvider lastModPropertyProvider = new LastModPropertyProvider();
    private final ChangeFreqPropertyProvider changeFreqPropertyProvider = new ChangeFreqPropertyProvider();
    private final PriorityPropertyProvider priorityPropertyProvider = new PriorityPropertyProvider();

    @Mock
    private SiteMapUrlBuilder urlBuilder;

    @Mock
    private ResourceResolverFactoryProxy resolverFactory;

    @Before
    public void setUp() throws NoSuchFieldException, LoginException {
        PrivateAccessor.setField(model, "pageRecognizer", pageRecognizer);
        PrivateAccessor.setField(model, "urlExternalizer", urlExternalizer);
        PrivateAccessor.setField(model, "lastModPropertyProvider", lastModPropertyProvider);
        PrivateAccessor.setField(model, "changeFreqPropertyProvider", changeFreqPropertyProvider);
        PrivateAccessor.setField(model, "priorityPropertyProvider", priorityPropertyProvider);
        PrivateAccessor.setField(model, "urlBuilder", urlBuilder);
        PrivateAccessor.setField(model, "resolverFactory", resolverFactory);
        when(resolverFactory.getServiceResourceResolver()).thenReturn(resourceResolver);
        model.activate();
    }

    @Test
    public void testGetters() {
        assertEquals(model, model.getConfiguration());
        assertNotNull(model.getPagePathPattern());
        assertEquals(pageRecognizer, model.getPageRecognizer());
        assertEquals(urlExternalizer, model.getUrlExternalizer());
        assertEquals(0, model.getXmlNamespaces().size());
        assertEquals(urlBuilder, model.getUrlBuilder());
        assertEquals(0, model.getDefaultPropertyProviders().size());
        final Collection<PropertyProvider> propertyProviders = model.getPropertyProviders();
        assertEquals(3, propertyProviders.size());
        assertTrue(propertyProviders.contains(lastModPropertyProvider));
        assertTrue(propertyProviders.contains(changeFreqPropertyProvider));
        assertTrue(propertyProviders.contains(priorityPropertyProvider));
    }

    @Test
    public void appliesTo() {
        assertFalse(model.appliesTo(null));
        assertTrue(model.appliesTo(page));
        templates.getContent().putProperty(DOMAINS, null);
        assertFalse(model.appliesTo(page));
    }

}