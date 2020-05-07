package com.peregrine.sitemap.impl;

import com.peregrine.mock.PageMock;
import com.peregrine.sitemap.PrefixAndCutUrlExternalizerBaseTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.commons.util.PerConstants.DOMAINS;
import static com.peregrine.commons.util.PerConstants.TEMPLATES;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultUrlExternalizerTest extends PrefixAndCutUrlExternalizerBaseTestBase<DefaultUrlExternalizer> {

    private final PageMock example = new PageMock("Example Root");
    private final PageMock pages = new PageMock("Pages Root");
    private final PageMock templates = new PageMock("Templates Root");

    public DefaultUrlExternalizerTest() {
        super(new DefaultUrlExternalizer());
        setPaths(PAGE_PATH, contentRoot, example, pages, parent, page);
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

    @Test
    public void map() {
        mapAndCompare("", "");
        basicTest();
        templates.getContent().putProperty(DOMAINS, new String[] {});
        mapAndCompare(_CONTENT_EXAMPLE_PAGES + ".html", _CONTENT_EXAMPLE_PAGES + ".html");
    }

}
