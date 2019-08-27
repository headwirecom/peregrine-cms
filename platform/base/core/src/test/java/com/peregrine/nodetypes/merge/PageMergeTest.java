package com.peregrine.nodetypes.merge;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.nodetypes.merge.PageMerge.CONTENT_TEMPLATES;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import com.peregrine.BindingsMock;
import com.peregrine.PageMock;
import com.peregrine.commons.util.BindingsUseUtil;

@RunWith(MockitoJUnitRunner.class)
public final class PageMergeTest {

    private static final String SLASH = "/";
    private static final String PAGE_PARENT_NAME = "parent";
    private static final String PAGE_PARENT_PATH = CONTENT_TEMPLATES + PAGE_PARENT_NAME;
    private static final String PAGE_NAME = "page";
    private static final String PAGE_PATH = PAGE_PARENT_PATH + SLASH + PAGE_NAME;

    private final PageMerge model = new PageMerge();

    private final PageMock page = new PageMock();

    private final PageMock parent = new PageMock();

    private final HashMap<Object, Object> exportedResourceMap = new HashMap<>();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Before
    public void setUp() throws ExportException, MissingExporterException {
        final BindingsMock bindings = new BindingsMock();
        bindings.put(BindingsUseUtil.REQUEST, request);
        final SlingScriptHelper sling = Mockito.mock(SlingScriptHelper.class);
        bindings.put(BindingsUseUtil.SLING, sling);
        when(sling.getService(ModelFactory.class)).thenReturn(modelFactory);

        when(request.getResource()).thenReturn(page.getContent());
        when(request.getResourceResolver()).thenReturn(resourceResolver);

        page.setPath(PAGE_PATH);
        page.setParent(parent);

        parent.setPath(PAGE_PARENT_PATH);
        parent.addChild(PAGE_NAME, page);

        when(modelFactory.exportModelForResource(any(), any(), any(), any())).thenReturn(exportedResourceMap);

        when(resourceResolver.getResource(PAGE_PARENT_PATH)).thenReturn(parent);

        model.init(bindings);
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
        equals("{\"fromTemplate\":true}");
    }

    @Test
    public void getMerged_emptyPageMap_missingParent() {
        page.setParent(null);
        equalsEmpty();
    }

    @Test
    public void getMerged_fromEmptyTemplate() throws ExportException, MissingExporterException {
        final String path = CONTENT_TEMPLATES + "empty";
        final PageMock template = new PageMock();
        when(resourceResolver.getResource(path)).thenReturn(template);
        exportedResourceMap.put(PageMerge.TEMPLATE, path);
        when(modelFactory.exportModelForResource(eq(template.getContent()), any(), any(), any())).thenReturn(new HashMap<>());
        equals("{\"fromTemplate\":true,\"template\":\"/content/templates/empty\"}");
    }

    @Test
    public void getMerged_flagFromTemplateRecursion() {
        exportedResourceMap.put("string", "string");
        final HashMap<Object, Object> map = new HashMap<>();
        map.put("string", "string");
        map.put("list", new ArrayList<>());
        final List<Object> list = new ArrayList<>();
        list.add(map);
        list.add("value");
        exportedResourceMap.put("list", list);

        equals("{\"fromTemplate\":true,\"list\":[{\"string\":\"string\",\"fromTemplate\":true,\"list\":[]},\"value\"],\"string\":\"string\"}");
    }

    @Test
    public void getMerged_specialValuesInMap() {
        exportedResourceMap.put("null", null);
        exportedResourceMap.put(COMPONENT, NT_UNSTRUCTURED);
        exportedResourceMap.put("map", new HashMap());
        equals("{\"component\":\"nt:unstructured\",\"fromTemplate\":true,\"map\":{},\"null\":null}");
    }

    @Test
    public void getMerged_componentNull() {
        exportedResourceMap.put(COMPONENT, null);
        equals("{\"component\":null,\"fromTemplate\":true}");
    }

    @Test
    public void getMerged_path() {
        final HashMap<Object, Object> map = new HashMap<>();
        map.put(PATH, "/path");
        final List<Object> list = new ArrayList<>();
        list.add(map);
        exportedResourceMap.put("list", list);

        equals("{\"fromTemplate\":true,\"list\":[{\"fromTemplate\":true,\"path\":\"/path\"}]}");
    }

    @Test
    public void getMerged_externalTemplate() throws ExportException, MissingExporterException {
        final String path = CONTENT_TEMPLATES + "external";
        final PageMock template = new PageMock();
        when(resourceResolver.getResource(path)).thenReturn(template);
        exportedResourceMap.put(PageMerge.TEMPLATE, path);
        when(modelFactory.exportModelForResource(eq(template.getContent()), any(), any(), any())).thenReturn(new HashMap<>());
        final HashMap<Object, Object> map = new HashMap<>();
        map.put(PATH, "/path");
        final List<Object> list = new ArrayList<>();
        list.add(map);
        exportedResourceMap.put("list", list);
        equals("{\"fromTemplate\":true,\"list\":[{\"path\":\"/path\"}],\"template\":\"/content/templates/external\"}");
    }
}