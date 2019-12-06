package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.HasName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class NamedServiceRetrieverImplTest {

    private static final String NAME = "name";

    private final NamedServiceRetrieverImpl model = new NamedServiceRetrieverImpl();
    private final List<ServiceReference<HasName>> services = new LinkedList<>();

    @Mock
    private BundleContext context;

    @Mock
    private HasName neededService;

    @Mock
    private ServiceReference<HasName> neededServiceRef;

    @Mock
    private HasName otherService;

    @Mock
    private ServiceReference<HasName> otherServiceRef;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws InvalidSyntaxException {
        model.activate(context);
        when(neededService.getName()).thenReturn(NAME);
        when(context.getService(neededServiceRef)).thenReturn(neededService);
        when(otherService.getName()).thenReturn("other");
        when(context.getService(otherServiceRef)).thenReturn(otherService);
        services.add(otherServiceRef);
        when(context.getServiceReferences(any(Class.class), any())).thenReturn(services);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void handleInvalidSyntaxException() throws InvalidSyntaxException {
        when(context.getServiceReferences(any(Class.class), any())).thenThrow(InvalidSyntaxException.class);
        assertNull(model.getNamedService(HasName.class, NAME));
    }

    @Test
    public void getNamedService() {
        assertNull(model.getNamedService(HasName.class, NAME));
        services.add(neededServiceRef);
        assertEquals(neededService, model.getNamedService(HasName.class, NAME));
    }

}
