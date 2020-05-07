package com.peregrine.mock;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RepoMock {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ResourceMock repoRoot = new ResourceMock("Repository Root");
    protected final ResourceMock contentRoot = new ResourceMock("Content Root");

    protected final ResourceResolverFactory resolverFactory = mock(ResourceResolverFactory.class, fullName("Resolver Factory"));
    protected final ResourceResolver resourceResolver = mock(ResourceResolver.class, fullName("Resource Resolver"));
    protected final Map<String, String> resourceResolverMap = new HashMap<>();
    protected final Session session = mock(Session.class, fullName("Session"));

    private final Map<String, ResourceMock> resolvableResources = new HashMap<>();

    public RepoMock() {
        repoRoot.setPath(SLASH);
        setPaths(SLASH, contentRoot);
        setParentChildRelationships(repoRoot, contentRoot);
        init(repoRoot);
        init(contentRoot);
        bindResolverFactory();
        when(resourceResolver.map(any())).thenAnswer(invocation -> resourceResolverMap.get(invocation.getArguments()[0]));
        mockResourceResolverCreate();
    }

    public static void setPaths(final String path, final ResourceMock... resources) {
        String currentPath = path;
        for (int i = resources.length - 1; i >= 0; i--) {
            resources[i].setPath(currentPath);
            currentPath = StringUtils.substringBeforeLast(currentPath, SLASH);
        }
    }

    public static void setParentChildRelationships(final ResourceMock... resources) {
        for (int i = 0; i < resources.length - 1; i++) {
            final ResourceMock parent = resources[i];
            final ResourceMock child = resources[i + 1];
            child.setParent(parent);
            parent.addChild(child);
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

    public <Mock extends ResourceMock> Mock init(final Mock mock) {
        mock.setResourceResolver(resourceResolver);
        mock.setSession(session);
        resolvableResources.put(mock.getPath(), mock);
        return mock;
    }

    public PageMock init(final PageMock mock) {
        init((ResourceMock)mock);
        init(mock.getContent());
        return mock;
    }

    protected String fullName(final String name) {
        return getClass().getSimpleName() + " " + name;
    }

    public ResourceMock getRepoRoot() {
        return repoRoot;
    }

    public ResourceMock getContentRoot() {
        return contentRoot;
    }

    public ResourceResolverFactory getResolverFactory() {
        return resolverFactory;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public Session getSession() {
        return session;
    }
}