package org.apache.sling.models.impl;

import org.apache.sling.models.spi.Injector;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.mockito.Mockito.when;

public class ModelAdapterFactorySetup {
    public static ModelAdapterFactory createModelAdapterFactory(Injector injector, Class...models) throws Exception {
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        ModelAdapterFactory factory = createModelAdapterFactory(bundleContext);
        if(injector != null) {
            factory.bindInjector(injector, new ServicePropertiesMap(0, 0));
        }
        factory.adapterImplementations.addClassesAsAdapterAndImplementation(models);
        return factory;
    }

    public static ModelAdapterFactory createModelAdapterFactory(BundleContext bundleContext) throws Exception {
        ComponentContext componentCtx = Mockito.mock(ComponentContext.class);
        when(componentCtx.getBundleContext()).thenReturn(bundleContext);
        when(componentCtx.getProperties()).thenReturn(new Hashtable<String, Object>());

        ModelAdapterFactory factory = new ModelAdapterFactory();
        Converter c = Converters.standardConverter();
        Map<String, String> map = new HashMap<>();
        factory.activate(componentCtx);
        return factory;
    }

    public static void addInjector(ModelAdapterFactory factory, Injector injector, int ranking) {
        factory.bindInjector(injector, new ServicePropertiesMap(ranking, ranking));
    }
}
