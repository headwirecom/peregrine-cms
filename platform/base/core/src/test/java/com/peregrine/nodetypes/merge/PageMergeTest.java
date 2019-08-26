package com.peregrine.nodetypes.merge;

import com.peregrine.BindingsMock;
import com.peregrine.commons.util.BindingsUseUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
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

import java.util.Collections;
import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class PageMergeTest {

    private static final String SLASH = "/";
    private static final String PAGE_PARENT_NAME = "parent";
    private static final String PAGE_NAME = "page";
    private static final String PAGE_PARENT_PATH = "/content/templates/" + PAGE_PARENT_NAME;
    private static final String PAGE_PATH = PAGE_PARENT_PATH + SLASH + PAGE_NAME;
    private static final String JCR_CONTENT_PATH = PAGE_PATH + SLASH + JCR_CONTENT;

    private final PageMerge model = new PageMerge();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private Resource page;

    @Mock
    private Resource jcrContent;

    @Mock
    private Resource pageParent;

    @Before
    public void setUp() {
        final BindingsMock bindings = new BindingsMock();
        bindings.put(BindingsUseUtil.REQUEST, request);
        final SlingScriptHelper sling = Mockito.mock(SlingScriptHelper.class);
        bindings.put(BindingsUseUtil.SLING, sling);
        when(sling.getService(ModelFactory.class)).thenReturn(modelFactory);
        model.init(bindings);

        when(request.getResource()).thenReturn(jcrContent);

        when(page.getName()).thenReturn(PAGE_NAME);
        when(page.getPath()).thenReturn(PAGE_PATH);
        when(page.getChild(JCR_CONTENT)).thenReturn(jcrContent);
        when(page.getParent()).thenReturn(pageParent);

        when(jcrContent.getName()).thenReturn(JCR_CONTENT);
        when(jcrContent.getPath()).thenReturn(JCR_CONTENT_PATH);
        when(jcrContent.getParent()).thenReturn(page);

        when(pageParent.getName()).thenReturn(PAGE_PARENT_NAME);
        when(pageParent.getPath()).thenReturn(PAGE_PARENT_PATH);
        when(pageParent.getResourceType()).thenReturn(PAGE_PRIMARY_TYPE);
        when(pageParent.getChild(PAGE_NAME)).thenReturn(page);
    }

    @Test
    public void getRenderContext() {
        Assert.assertEquals(request, PageMerge.getRenderContext().getRequest());
    }

    private void equals(final String expected) {
        final String actual = model.getMergedForScript();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getMerged_missingJcrContent() {
        when(request.getResource()).thenReturn(page);
        when(page.getChild(JCR_CONTENT)).thenReturn(null);
        equals("{}");
    }

	@Test
    @SuppressWarnings("unchecked")
    public void getMerged_ExportException() throws ExportException, MissingExporterException {
    	when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenThrow(ExportException.class);
        equals("{}");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getMerged_MissingExporterException() throws ExportException, MissingExporterException {
        when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenThrow(MissingExporterException.class);
        equals("{}");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getMerged_emptyPageMap() throws ExportException, MissingExporterException {
        when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenReturn(Collections.emptyMap());
        when(page.getParent()).thenReturn(null);
        equals("{}");
    }
}