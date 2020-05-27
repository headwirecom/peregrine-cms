package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import com.peregrine.mock.SiteMock;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.mock.MockTools.setParentChildRelationships;
import static com.peregrine.mock.MockTools.setPaths;

public class SiteStructureTestBase extends SlingResourcesTest {

    protected static final String EXAMPLE_COM = "http://www.example.com";
    protected static final String _CONTENT_EXAMPLE_ = "/content/example/";
    protected static final String _CONTENT_EXAMPLE_PAGES = _CONTENT_EXAMPLE_ + "pages";
    protected static final String _CONTENT_EXAMPLE_PAGES_ = _CONTENT_EXAMPLE_PAGES + SLASH;
    protected static final String PAGE_PATH = _CONTENT_EXAMPLE_PAGES_ + "parent/page";

    protected final SiteMock example = repo.init(new SiteMock("example"));
    protected final ResourceMock parent = new ResourceMock("Parent");
    protected final PageMock page = new PageMock("Page");

    public SiteStructureTestBase() {
        setPaths(PAGE_PATH, parent, page);
        setParentChildRelationships(example.getPages(), parent, page);
        init(parent);
        init(page);
        example.setDomains(EXAMPLE_COM);
    }

}
