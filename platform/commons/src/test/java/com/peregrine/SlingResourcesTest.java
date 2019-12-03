package com.peregrine;

import com.peregrine.commons.util.PerConstants;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlingResourcesTest {

    protected static final String NN_ROOT = PerConstants.NN_CONTENT;
    protected static final String NN_PARENT = "parent";
    protected static final String NN_PAGE = "page";
    protected static final String NN_RESOURCE = "resource";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String RESOURCE_TYPE = "per/component";
    public static final String SLASH_APPS_SLASH = APPS_ROOT + SLASH;

    protected final ResourceMock repoRoot = new ResourceMock("Repository Root");
    protected final ResourceMock root = new ResourceMock("Root");
    protected final ResourceMock parent = new ResourceMock("Parent");
    protected final PageMock page = new PageMock("Page");
    protected final ResourceMock content = page.getContent();
    protected final ResourceMock resource = new ResourceMock("Resource");

    protected final List<ResourceMock> resources = Arrays.asList(root, parent, page, content, resource);

    protected final ResourceResolverFactory resolverFactory = mock(ResourceResolverFactory.class, fullName("Resolver Factory"));
    protected final ResourceResolver resourceResolver = mock(ResourceResolver.class, fullName("Resource Resolver"));
    protected final Session session = mock(Session.class, fullName("Session"));

    protected final PageMock component = new PageMock("Per Component");

    protected final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class, fullName("Request"));

    public SlingResourcesTest() {
        setPaths();
        setParentChildRelationships();
        initResources();
        component.setPath(SLASH_APPS_SLASH + RESOURCE_TYPE);
        init(component);
        bindResolverFactory();
        bindRequest();
    }

    private void setPaths() {
        String path = SLASH;
        repoRoot.setPath(path);
        path += NN_ROOT;
        root.setPath(path);
        path += SLASH + NN_PARENT;
        parent.setPath(path);
        path += SLASH + NN_PAGE;
        page.setPath(path);
        path = content.getPath();
        path += SLASH + NN_RESOURCE;
        resource.setPath(path);
    }

    private void setParentChildRelationships() {
        root.setParent(repoRoot);
        parent.setParent(root);
        page.setParent(parent);
        resource.setParent(content);

        repoRoot.addChild(root);
        root.addChild(parent);
        parent.addChild(page);
        content.addChild(resource);
    }

    private void initResources() {
        init(repoRoot);
        for (final ResourceMock mock: resources) {
            init(mock);
        }
    }

    private void bindResolverFactory() {
        try {
            when(resolverFactory.getServiceResourceResolver(Mockito.any())).thenReturn(resourceResolver);
            when(resolverFactory.getResourceResolver(Mockito.any())).thenReturn(resourceResolver);
            when(resolverFactory.getThreadResourceResolver()).thenReturn(resourceResolver);
        } catch (final LoginException e) {
        }
    }

    private void bindRequest() {
        when(request.getResource()).thenReturn(resource);
        when(request.getResourceResolver()).thenReturn(resourceResolver);
    }

    public Logger getLogger() {
        return logger;
    }

    protected <Mock extends ResourceMock> Mock init(final Mock mock) {
        mock.setResourceResolver(resourceResolver);
        mock.setSession(session);
        return mock;
    }

    private String fullName(final String name) {
        return SlingResourcesTest.class.getSimpleName() + " " + name;
    }
}
