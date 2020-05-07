package com.peregrine.mock;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.commons.util.PerConstants.*;

public final class SiteStructureMock {

    private final PageMock site = new PageMock("Site Root");
    {
        site.setPrimaryType(SITE_PRIMARY_TYPE);
    }
    private final PageMock pages = new PageMock("Pages Root");
    private final PageMock templates = new PageMock("Templates Root");

    private final RepoMock repo;

    public SiteStructureMock(final RepoMock repo, final String name) {
        this.repo = repo;
        RepoMock.setPaths(CONTENT_ROOT + SLASH + name + SLASH + PAGES, site, pages);
        RepoMock.setPaths(CONTENT_ROOT + SLASH + name + SLASH + TEMPLATES, site, templates);
        repo.init(site);
        repo.init(pages);
        repo.init(templates);
    }

}
