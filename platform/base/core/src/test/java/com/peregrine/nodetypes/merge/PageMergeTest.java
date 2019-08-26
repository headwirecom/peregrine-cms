package com.peregrine.nodetypes.merge;

import com.peregrine.BindingsMock;
import com.peregrine.PageMock;
import com.peregrine.commons.util.BindingsUseUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PageMergeTest {

    private static final String SLASH = "/";
    private static final String PAGE_PARENT_NAME = "parent";
    private static final String PAGE_PARENT_PATH = "/content/templates/" + PAGE_PARENT_NAME;
    private static final String PAGE_NAME = "page";
    private static final String PAGE_PATH = PAGE_PARENT_PATH + SLASH + PAGE_NAME;

    private final PageMerge model = new PageMerge();

    private final PageMock page = new PageMock();

    private final PageMock parent = new PageMock();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private ModelFactory modelFactory;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws ExportException, MissingExporterException {
        final BindingsMock bindings = new BindingsMock();
        bindings.put(BindingsUseUtil.REQUEST, request);
        final SlingScriptHelper sling = Mockito.mock(SlingScriptHelper.class);
        bindings.put(BindingsUseUtil.SLING, sling);
        when(sling.getService(ModelFactory.class)).thenReturn(modelFactory);
        model.init(bindings);

        when(request.getResource()).thenReturn(page.getContent());
        final ResourceResolver resourceResolver = Mockito.mock(ResourceResolver.class);
        when(request.getResourceResolver()).thenReturn(resourceResolver);

        page.setPath(PAGE_PATH);
        page.setParent(parent);

        parent.setPath(PAGE_PARENT_PATH);
        parent.addChild(PAGE_NAME, page);

        when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenReturn(new HashMap<>());

        when(resourceResolver.getResource(PAGE_PARENT_PATH)).thenReturn(parent);
    }

    @Test
    public void getRenderContext() {
        Assert.assertEquals(request, PageMerge.getRenderContext().getRequest());
    }

    private void equals(final String expected) {
        final String actual = model.getMergedForScript();
        Assert.assertEquals(expected, actual);
    }

    private void equalsEmpty() {
        equals("{}");
    }

    @Test
    public void getMerged_missingJcrContent() {
        when(request.getResource()).thenReturn(page);
        page.addChild(JCR_CONTENT, null);
        equalsEmpty();
    }

	@Test
    @SuppressWarnings("unchecked")
    public void getMerged_ExportException() throws ExportException, MissingExporterException {
    	when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenThrow(ExportException.class);
        equalsEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getMerged_MissingExporterException() throws ExportException, MissingExporterException {
        when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenThrow(MissingExporterException.class);
        equalsEmpty();
    }

    @Test
    public void getMerged_emptyPageMap() {
        page.setParent(null);
        equalsEmpty();
    }
}