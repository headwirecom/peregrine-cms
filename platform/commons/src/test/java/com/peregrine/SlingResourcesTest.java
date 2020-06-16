package com.peregrine;

import com.peregrine.mock.PageMock;
import com.peregrine.mock.RepoMock;
import com.peregrine.mock.ResourceMock;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.peregrine.commons.Chars.DOT;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.mock.MockTools.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlingResourcesTest {

    public static final String RESOURCE_TYPE = "per/component";
    public static final String SLASH_APPS_SLASH = APPS_ROOT + SLASH;

    protected static final String NN_PARENT = "parent";
    protected static final String NN_PAGE = "page";
    protected static final String NN_RESOURCE = "resource";
    protected static final String PAGE_PATH = CONTENT_ROOT + SLASH + NN_PARENT + SLASH + NN_PAGE;

    protected final ResourceMock parent = new ResourceMock("Parent");
    protected final PageMock page = new PageMock("Page");
    protected final ResourceMock jcrContent = page.getContent();
    protected final ResourceMock resource = new ResourceMock("Resource");

    protected final PageMock component = new PageMock("Per Component");

    protected final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class, fullName(this, "Request"));
    protected final RequestPathInfo requestPathInfo = Mockito.mock(RequestPathInfo.class);

    protected final RepoMock repo;
    protected final ResourceMock contentRoot;
    protected final ResourceResolver resourceResolver;

    protected final List<ResourceMock> resources;

    public SlingResourcesTest(final RepoMock repo) {
        this.repo = repo;
        contentRoot = repo.getContent();
        resourceResolver = repo.getResourceResolver();
        resources = Arrays.asList(contentRoot, parent, page, jcrContent, resource);
        setPaths(PAGE_PATH, contentRoot, parent, page);
        resource.setPath(jcrContent.getPath() + SLASH + NN_RESOURCE);
        setParentChildRelationships(contentRoot, parent, page);
        setParentChildRelationships(jcrContent, resource);
        for (final ResourceMock mock: resources) {
            init(mock);
        }

        component.setPath(SLASH_APPS_SLASH + RESOURCE_TYPE);
        resource.setResourceType(RESOURCE_TYPE);
        init(component);
        bindRequest();
    }

    public SlingResourcesTest() {
        this(new RepoMock());
    }

    private void bindRequest() {
        when(request.getResource()).thenReturn(resource);
        when(request.getResourceResolver()).thenReturn(resourceResolver);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        final String path = resource.getPath();
        when(requestPathInfo.getResourcePath()).thenReturn(path);
        when(requestPathInfo.getExtension()).thenReturn(HTML);
    }

    protected <Mock extends ResourceMock> Mock init(final Mock mock) {
        return repo.init(mock);
    }

    protected PageMock init(final PageMock mock) {
        return repo.init(mock);
    }

    protected void setSelectors(final String... selectors) {
        when(requestPathInfo.getSelectors()).thenReturn(selectors);
        when(requestPathInfo.getSelectorString()).thenReturn(StringUtils.join(selectors, DOT));
    }

    protected void setSelectorsString(final String selectorsString) {
        when(requestPathInfo.getSelectors()).thenReturn(selectorsString.split("\\."));
        when(requestPathInfo.getSelectorString()).thenReturn(selectorsString);
    }

}
