package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.RepoMock;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.commons.util.PerConstants.DOMAINS;
import static com.peregrine.commons.util.PerConstants.TEMPLATES;

public class SiteStructureTestBase extends SlingResourcesTest {

    protected static final String EXAMPLE_COM = "http://www.example.com";
    protected static final String _CONTENT_EXAMPLE_ = "/content/example/";
    protected static final String _CONTENT_EXAMPLE_PAGES = _CONTENT_EXAMPLE_ + "pages";
    protected static final String _CONTENT_EXAMPLE_PAGES_ = _CONTENT_EXAMPLE_PAGES + SLASH;
    protected static final String PAGE_PATH = _CONTENT_EXAMPLE_PAGES_ + "parent/page";

    protected final PageMock example = new PageMock("Example Root");
    protected final PageMock pages = new PageMock("Pages Root");
    protected final PageMock templates = new PageMock("Templates Root");

    public SiteStructureTestBase() {
        RepoMock.setPaths(PAGE_PATH, contentRoot, example, pages, parent, page);
        init(contentRoot);
        init(example);
        init(pages);
        init(parent);
        init(page);
        example.addChild(TEMPLATES, templates);
        templates.setPath(example.getPath() + SLASH + TEMPLATES);
        templates.setParent(example);
        templates.getContent().putProperty(DOMAINS, EXAMPLE_COM);
        init(templates);
    }

}
