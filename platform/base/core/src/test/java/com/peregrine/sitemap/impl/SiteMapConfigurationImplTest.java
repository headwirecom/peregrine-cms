package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import com.peregrine.sitemap.*;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapConfigurationImplTest extends SlingResourcesTest {

    public static final String X = "x";
    public static final String Y = "y";
    private final SiteMapConfigurationImpl model = new SiteMapConfigurationImpl();

    @Mock
    private SiteMapConfigurationsContainer container;

    @Mock
    private NamedServiceRetriever serviceRetriever;

    @Mock
    private SiteMapConfigurationImplConfig config;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "container", container);
        PrivateAccessor.setField(model, "serviceRetriever", serviceRetriever);
        when(config.enabled()).thenReturn(true);
        model.activate(config);
    }

	@Test
    public void enabled() {
        verify(container, times(1)).add(model);
        model.deactivate();
        verify(container, times(1)).remove(model);
    }

    @Test
    public void disabled() {
        verify(container, times(1)).add(model);
        activateDisabled();
        verify(container, times(1)).add(model);
        model.deactivate();
        verify(container, times(1)).remove(model);
    }

    private void activateDisabled() {
        when(config.enabled()).thenReturn(false);
        model.activate(config);
    }

    @Test
    public void getPagePathPattern() {
        when(config.pathRegex()).thenReturn("/content/example/pages/.+");
        final Pattern pattern = model.getPagePathPattern();
        assertNotNull(pattern);
        assertFalse(pattern.matcher("/content/example/pages/").matches());
        assertTrue(pattern.matcher("/content/example/pages/page").matches());
    }

    @Test
    public void getPageRecognizer() {
        when(config.pageRecognizers()).thenReturn(new String[] {X, Y});
        PageRecognizer pageRecognizer = model.getPageRecognizer();
        assertNotNull(pageRecognizer);
        final Page page = new Page(this.page);
        assertTrue(pageRecognizer.isPage(page));
        pageRecognizer = mock(PageRecognizer.class);
        when(pageRecognizer.isPage(page)).thenReturn(false);
        when(serviceRetriever.getNamedService(PageRecognizer.class, X)).thenReturn(pageRecognizer);
        pageRecognizer = model.getPageRecognizer();
        assertFalse(pageRecognizer.isPage(page));
    }

    @Test
    public void getUrlExternalizer() {
        when(config.urlExternalizer()).thenReturn(X);
        assertNull(model.getUrlExternalizer());
        final UrlExternalizer externalizer = mock(UrlExternalizer.class);
        when(serviceRetriever.getNamedService(UrlExternalizer.class, X)).thenReturn(externalizer);
        assertEquals(externalizer, model.getUrlExternalizer());
    }

    @Test
    public void getXmlNamespaces() {
        when(config.xmlnsMappings()).thenReturn(new String[] { "x=a", Y });
        final Map<String, String> xmlNamespaces = model.getXmlNamespaces();
        assertNotNull(xmlNamespaces);
        assertTrue(xmlNamespaces.containsKey("xmlns:x"));
        assertEquals("a", xmlNamespaces.get("xmlns:x"));
        assertFalse(xmlNamespaces.containsKey("xmlns:y"));
    }

    @Test
    public void getPropertyProviders() {
        Collection<PropertyProvider> propertyProviders = model.getPropertyProviders();
        assertNotNull(propertyProviders);
        assertTrue(propertyProviders.isEmpty());
        when(config.propertyProviders()).thenReturn(new String[] { X, Y });
        when(serviceRetriever.getNamedService(PropertyProvider.class, X)).thenReturn(mock(PropertyProvider.class));
        when(serviceRetriever.getNamedService(PropertyProvider.class, Y)).thenReturn(mock(PropertyProvider.class));
        propertyProviders = model.getPropertyProviders();
        assertNotNull(propertyProviders);
        assertFalse(propertyProviders.isEmpty());
        assertEquals(2, propertyProviders.size());
    }

    @Test
    public void getMandatoryCachedPaths() {
        when(config.mandatoryCachedRootPaths()).thenReturn(new String[] { X, Y });
        Set<String> paths = model.getMandatoryCachedPaths();
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.contains(X));
        assertTrue(paths.contains(Y));
        activateDisabled();
        paths = model.getMandatoryCachedPaths();
        assertNotNull(paths);
        assertTrue(paths.isEmpty());
    }

}
