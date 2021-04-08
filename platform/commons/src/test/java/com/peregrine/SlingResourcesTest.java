package com.peregrine;

import com.peregrine.mock.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.mock.MockTools.*;
import static org.apache.commons.lang3.StringUtils.*;

public class SlingResourcesTest {

    public static final String RESOURCE_TYPE = "per/component";
    public static final String SLASH_APPS_SLASH = APPS_ROOT + SLASH;

    protected static final String NN_PARENT = "parent";
    protected static final String NN_PAGE = "page";
    protected static final String NN_RESOURCE = "resource";

    protected final ResourceMock parent = new ResourceMock("Parent");
    protected final PageMock page = new PageMock("Page");
    protected final PageContentMock jcrContent = page.getContent();
    protected final ResourceMock resource = new ResourceMock("Resource");

    protected final PageMock component = new PageMock("Per Component");

    protected final SlingHttpServletRequestMock request = new SlingHttpServletRequestMock(fullName(this, "Request"));

    protected final RepoMock repo;
    protected final ResourceMock contentRoot;
    protected final ResourceResolver resourceResolver;

    protected final List<ResourceMock> resources;

    public SlingResourcesTest(final RepoMock repo, final String rootPath) {
        this.repo = repo;
        contentRoot = repo.getContent();
        resourceResolver = repo.getResourceResolver();
        final String contentRootPathPrefix = contentRoot.getPath() + SLASH;
        final String relRootPath = substringAfter(rootPath, contentRootPathPrefix);
        resources = Arrays.asList(relRootPath.split(SLASH)).stream()
                .filter(StringUtils::isNotBlank)
                .map(ResourceMock::new)
                .collect(Collectors.toList());
        resources.add(0, contentRoot);
        resources.add(parent);
        resources.add(page);
        setPaths(contentRootPathPrefix
                        + (isNotBlank(relRootPath) ? relRootPath + SLASH : EMPTY)
                        + NN_PARENT + SLASH + NN_PAGE,
                resources.toArray(new ResourceMock[resources.size()]));
        resources.add(jcrContent);
        resources.add(resource);
        resource.setPath(jcrContent.getPath() + SLASH + NN_RESOURCE);
        setParentChildRelationships(contentRoot, parent, page);
        setParentChildRelationships(jcrContent, resource);
        for (final ResourceMock mock: resources) {
            init(mock);
        }

        component.setPath(SLASH_APPS_SLASH + RESOURCE_TYPE);
        resource.setResourceType(RESOURCE_TYPE);
        init(component);
        request.bind(resource);
    }

    public SlingResourcesTest(final String rootPath) {
        this(new RepoMock(), rootPath);
    }

    public SlingResourcesTest() {
        this(CONTENT_ROOT);
    }

    protected <Mock extends ResourceMock> Mock init(final Mock mock) {
        return repo.init(mock);
    }

    protected PageMock init(final PageMock mock) {
        return repo.init(mock);
    }

}
