package com.peregrine;

import com.peregrine.commons.util.PerConstants;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.Chars.DOT;
import static com.peregrine.commons.util.PerConstants.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlingResourcesTest {

    public static final String RESOURCE_TYPE = "per/component";
    public static final String SLASH_APPS_SLASH = APPS_ROOT + SLASH;

    protected static final String NN_ROOT = PerConstants.NN_CONTENT;
    protected static final String NN_PARENT = "parent";
    protected static final String NN_PAGE = "page";
    protected static final String NN_RESOURCE = "resource";
    protected static final String PAGE_PATH = SLASH + NN_ROOT + SLASH + NN_PARENT + SLASH + NN_PAGE;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ResourceMock repoRoot = new ResourceMock("Repository Root");
    protected final ResourceMock root = new ResourceMock("Root");
    protected final ResourceMock parent = new ResourceMock("Parent");
    protected final PageMock page = new PageMock("Page");
    protected final ResourceMock content = page.getContent();
    protected final ResourceMock resource = new ResourceMock("Resource");

    protected final List<ResourceMock> resources = Arrays.asList(root, parent, page, content, resource);

    protected final ResourceResolverFactory resolverFactory = mock(ResourceResolverFactory.class, fullName("Resolver Factory"));
    protected final ResourceResolver resourceResolver = mock(ResourceResolver.class, fullName("Resource Resolver"));
    protected final Map<String, String> resourceResolverMap = new HashMap<>();
    protected final Session session = mock(Session.class, fullName("Session"));

    protected final PageMock component = new PageMock("Per Component");

    protected final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class, fullName("Request"));
    protected final RequestPathInfo requestPathInfo = Mockito.mock(RequestPathInfo.class);

    private final Map<String, ResourceMock> resolvableResources = new HashMap<>();

    public SlingResourcesTest() {
        setPaths();
        setParentChildRelationships();
        initResources();
        component.setPath(SLASH_APPS_SLASH + RESOURCE_TYPE);
        resource.setResourceType(RESOURCE_TYPE);
        init(component);
        bindResolverFactory();
        bindRequest();
        when(resourceResolver.map(any())).thenAnswer(invocation -> resourceResolverMap.get(invocation.getArguments()[0]));
    }

    private void setPaths() {
        repoRoot.setPath(SLASH);
        setPaths(PAGE_PATH, root, parent, page);
        resource.setPath(content.getPath() + SLASH + NN_RESOURCE);
    }

    protected static void setPaths(final String path, final ResourceMock... resources) {
        String currentPath = path;
        for (int i = resources.length - 1; i >= 0; i--) {
            resources[i].setPath(currentPath);
            currentPath = StringUtils.substringBeforeLast(currentPath, SLASH);
        }
    }

    private void setParentChildRelationships() {
        setParentChildRelationships(repoRoot, root, parent, page);
        setParentChildRelationships(content, resource);
    }

    protected static void setParentChildRelationships(final ResourceMock... resources) {
        for (int i = 0; i < resources.length - 1; i++) {
            final ResourceMock parent = resources[i];
            final ResourceMock child = resources[i + 1];
            child.setParent(parent);
            parent.addChild(child);
        }
    }

    private void initResources() {
        init(repoRoot);
        for (final ResourceMock mock: resources) {
            init(mock);
        }
    }

    private void bindResolverFactory() {
        try {
            when(resolverFactory.getServiceResourceResolver(any())).thenReturn(resourceResolver);
            when(resolverFactory.getResourceResolver(any())).thenReturn(resourceResolver);
            when(resolverFactory.getThreadResourceResolver()).thenReturn(resourceResolver);
        } catch (final LoginException e) {
        }
    }

    private void bindRequest() {
        when(request.getResource()).thenReturn(resource);
        when(request.getResourceResolver()).thenReturn(resourceResolver);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        final String path = resource.getPath();
        when(requestPathInfo.getResourcePath()).thenReturn(path);
        when(requestPathInfo.getExtension()).thenReturn(HTML);
    }

    @SuppressWarnings("unchecked")
	protected void mockResourceResolverCreate() {
        try {
            when(resourceResolver.create(any(ResourceMock.class), anyString(), any(Map.class))).thenAnswer(invocation -> {
                final Object[] args = invocation.getArguments();
                int index = 0;
                final ResourceMock parent = (ResourceMock) args[index++];
                final String name = (String) args[index++];
                final Map<String, Object> properties = (Map<String, Object>) args[index++];

                final String path = parent.getPath() + SLASH + name;
                final ResourceMock result;
                if (resolvableResources.containsKey(path)) {
                    result = resolvableResources.get(path);
                } else {
                    result = new ResourceMock(name);
                    result.setPath(path);
                }

                result.setParent(parent);
                result.putProperties(properties);
                return init(result);
            });
        } catch (final PersistenceException e) {
        }
    }

    public Logger getLogger() {
        return logger;
    }

    protected <Mock extends ResourceMock> Mock init(final Mock mock) {
        mock.setResourceResolver(resourceResolver);
        mock.setSession(session);
        resolvableResources.put(mock.getPath(), mock);
        return mock;
    }

    protected PageMock init(final PageMock mock) {
        init((ResourceMock)mock);
        init(mock.getContent());
        return mock;
    }

    protected String fullName(final String name) {
        return SlingResourcesTest.class.getSimpleName() + " " + name;
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
