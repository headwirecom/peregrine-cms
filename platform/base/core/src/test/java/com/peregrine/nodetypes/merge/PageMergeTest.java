package com.peregrine.nodetypes.merge;

import com.peregrine.BindingsMock;
import com.peregrine.commons.util.BindingsUseUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageMergeTest {

    private final PageMerge model = new PageMerge();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingScriptHelper sling;

    @Mock
    private ModelFactory modelFactory;

    @Before
    public void setUp() {
        final BindingsMock bindings = new BindingsMock();
        bindings.put(BindingsUseUtil.REQUEST, request);
        bindings.put(BindingsUseUtil.SLING, sling);
        when(sling.getService(ModelFactory.class)).thenReturn(modelFactory);
        model.init(bindings);
    }

    @Test
    public void getRenderContext() {
        Assert.assertEquals(request, PageMerge.getRenderContext().getRequest());
    }
}