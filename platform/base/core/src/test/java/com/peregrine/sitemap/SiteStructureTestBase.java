package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.SiteMock;
import com.peregrine.versions.VersioningResourceResolver;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.mock.MockTools.setParentChildRelationships;

public class SiteStructureTestBase extends SlingResourcesTest {

    protected static final String EXAMPLE_COM = "http://www.example.com";
    protected static final String _CONTENT_EXAMPLE_PAGES = "/content/example/pages";
    protected static final String _CONTENT_EXAMPLE_PAGES_ = _CONTENT_EXAMPLE_PAGES + SLASH;
    protected static final String PAGE_PATH = _CONTENT_EXAMPLE_PAGES_ + "parent/page";

    protected final VersioningResourceResolver versioningResolver
            = new VersioningResourceResolver(resourceResolver, resourceResolver::getResource);
    protected final SiteMock example = repo.init(new SiteMock("example"));

    public SiteStructureTestBase() {
        super(_CONTENT_EXAMPLE_PAGES);
        setParentChildRelationships(example.getPages(), parent);
        example.setDomains(EXAMPLE_COM);
    }

}
