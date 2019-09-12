package com.peregrine.commons.test;

import com.peregrine.commons.test.mock.PageMock;
import com.peregrine.commons.test.mock.ResourceMock;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Mockito.mock;

public class SlingResourcesTest extends AbstractTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String PRIMARY_TYPE = "per:Type";
    public static final String RESOURCE_TYPE = "per/component";

    protected final ResourceMock root = new ResourceMock();
    protected final ResourceMock parent = new ResourceMock();
    protected final PageMock page = new PageMock();
    protected final ResourceMock content = page.getContent();
    protected final ResourceMock resource = new ResourceMock();

    protected final List<Resource> resources = new LinkedList<>();

    protected final ResourceResolverFactory resolverFactory = mock(ResourceResolverFactory.class);
    protected final ResourceResolver resourceResolver = mock(ResourceResolver.class);

    public SlingResourcesTest() {
        String path = SLASH + "content";
        root.setPath(path);
        path += SLASH + "parent";
        parent.setPath(path);
        path += SLASH + "page";
        page.setPath(path);
        path = content.getPath();
        path += SLASH + "resource";
        resource.setPath(path);

        parent.setParent(root);
        page.setParent(parent);
        resource.setParent(content);

        root.addChild(parent);
        parent.addChild(page);
        content.addChild(resource);

        init(root);
        init(parent);
        init(page);
        init(content);
        init(resource);

        resources.add(root);
        resources.add(parent);
        resources.add(page);
        resources.add(content);
        resources.add(resource);
    }

    public Logger getLogger() {
        return logger;
    }

    protected <Mock extends ResourceMock> Mock init(final Mock mock) {
        mock.setResourceResolver(resourceResolver);
        return mock;
    }
}
