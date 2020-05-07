package com.peregrine.mock;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.mock.MockTools.setParentChildRelationships;
import static com.peregrine.mock.MockTools.setPaths;

public final class SiteMock extends PageMock {

    private final PageMock pages = new PageMock("Pages Root");
    private final PageMock templates = new PageMock("Templates Root");

    public SiteMock(final String name) {
        setPrimaryType(SITE_PRIMARY_TYPE);
        setPaths(CONTENT_ROOT + SLASH + name + SLASH + PAGES, this, pages);
        setPaths(CONTENT_ROOT + SLASH + name + SLASH + TEMPLATES, this, templates);
        setParentChildRelationships(this, pages);
        setParentChildRelationships(this, templates);
    }

    public PageMock getPages() {
        return pages;
    }

    public PageMock getTemplates() {
        return templates;
    }

    public void setDomains(final String... domains) {
        templates.getContent().putProperty(DOMAINS, domains);
    }

}
