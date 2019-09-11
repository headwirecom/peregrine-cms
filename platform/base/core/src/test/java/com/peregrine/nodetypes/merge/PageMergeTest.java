package com.peregrine.nodetypes.merge;

import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.PATH;
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
    private static final String EXTERNAL_TEMPLATE_PATH = CONTENT_TEMPLATES + "external";

    private static final String PN_LIST = "list";

    private final PageMerge model = new PageMerge();

    private final BindingsMock bindings = new BindingsMock();

    private final PageMock page = new PageMock();

    private final PageMock parent = new PageMock();

    private final HashMap<Object, Object> exportedResourceMap = new HashMap<>();

    private final PageMock externalTemplate = new PageMock();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Before
    public void setUp() throws ExportException, MissingExporterException {
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

        when(resourceResolver.getResource(EXTERNAL_TEMPLATE_PATH)).thenReturn(externalTemplate);
        when(modelFactory.exportModelForResource(eq(externalTemplate.getContent()), any(), any(), any()))
                .thenReturn(externalTemplate.getProperties());
    }

    @Test
    public void getRenderContext() {
        model.init(bindings);
        Assert.assertEquals(request, PageMerge.getRenderContext().getRequest());
    }

    private void equals(final String expected) {
        model.init(bindings);
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
        map.put(PN_LIST, new ArrayList<>());
        final List<Object> list = new ArrayList<>();
        list.add(map);
        list.add("value");
        exportedResourceMap.put(PN_LIST, list);

        equals("{\"fromTemplate\":true,\"list\":[{\"string\":\"string\",\"fromTemplate\":true,\"list\":[]},\"value\"],\"string\":\"string\"}");
    }

    @Test
    public void getMerged_specialValuesInMap() {
        exportedResourceMap.put("null", null);
        exportedResourceMap.put(COMPONENT, NT_UNSTRUCTURED);
        exportedResourceMap.put("map", new HashMap<>());
        equals("{\"component\":\"nt:unstructured\",\"fromTemplate\":true,\"map\":{},\"null\":null}");
    }

    @Test
    public void getMerged_componentNull() {
        exportedResourceMap.put(COMPONENT, null);
        equals("{\"component\":null,\"fromTemplate\":true}");
    }

    @Test
    public void getMerged_path() {
        final List<Object> list = new ArrayList<>();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("string", "string");
        map.put(PATH, "/path");
        list.add(map);
        map = new HashMap<>();
        map.put(PATH, "/path2");
        list.add(map);
        exportedResourceMap.put(PN_LIST, list);

        equals("{\"fromTemplate\":true,\"list\":[{\"fromTemplate\":true,\"path\":\"/path\",\"string\":\"string\"},{\"fromTemplate\":true,\"path\":\"/path2\"}]}");
    }

    @Test
    public void getMerged_externalTemplate() {
        exportedResourceMap.put(PageMerge.TEMPLATE, EXTERNAL_TEMPLATE_PATH);
        final HashMap<Object, Object> map = new HashMap<>();
        map.put(PATH, "/path");
        final List<Object> list = new ArrayList<>();
        list.add(map);
        exportedResourceMap.put(PN_LIST, list);
        equals("{\"fromTemplate\":true,\"list\":[{\"path\":\"/path\"}],\"template\":\"/content/templates/external\"}");
    }

    @Test
    public void getMerged_incompatibleListTypes() {
        exportedResourceMap.put(PageMerge.TEMPLATE, EXTERNAL_TEMPLATE_PATH);
        externalTemplate.getProperties().put(PN_LIST, "Not a List");
        exportedResourceMap.put(PN_LIST, new ArrayList<>());
        equals("{\"fromTemplate\":true,\"list\":\"Not a List\",\"template\":\"/content/templates/external\"}");
    }

    @Test
    public void getMerged_addNotMergedToList() {
        exportedResourceMap.put(PageMerge.TEMPLATE, EXTERNAL_TEMPLATE_PATH);
        final List<Object> targetList = new ArrayList<>();
        targetList.add("x");
        externalTemplate.getProperties().put(PN_LIST, targetList);
        final List<Object> sourceList = new ArrayList<>();
        sourceList.add("y");
        exportedResourceMap.put(PN_LIST, sourceList);
        equals("{\"fromTemplate\":true,\"list\":[\"x\",\"y\"],\"template\":\"/content/templates/external\"}");
    }
}