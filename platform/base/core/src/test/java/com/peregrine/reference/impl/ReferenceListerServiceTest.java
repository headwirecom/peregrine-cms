package com.peregrine.reference.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageContentMock;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
	public final class ReferenceListerServiceTest extends SlingResourcesTest {

	@Mock
	private ReferenceListerService.Configuration configuration;

	private final ReferenceListerService model = new ReferenceListerService();
	private final PageMock siblingPage = new PageMock();
	final ResourceMock child = new ResourceMock();

	@Before
	public void setUp() {
		when(configuration.referencedByRoot()).thenReturn(new String[] { "/content/", null, EMPTY });
		when(configuration.referencePrefix()).thenReturn(new String[] { "/content", null });
		model.activate(configuration);

		siblingPage.setPath(parent.getPath() + SLASH + "sibling");
		parent.addChild(siblingPage);
		final PageContentMock content = siblingPage.getContent();
		final String name = "resource";
		child.setPath(content.getPath() + SLASH + name);
		content.addChild(name, child);
	}

	@Test
	public void modified() {
		model.modified(configuration);
	}

	@Test
	public void getReferenceList() {
		resource.putProperty("reference", siblingPage.getPath());
		child.putProperty("reference", page.getPath());
		var references = model.getReferenceList(true, page, true);
		assertTrue(references.contains(siblingPage));
		assertTrue(references.contains(page));
		references = model.getReferenceList(true, page, false);
		assertTrue(references.contains(siblingPage));
		assertTrue(references.contains(page));
		references = model.getReferenceList(false, page, true);
		assertTrue(references.contains(siblingPage));
		assertFalse(references.contains(page));
		references = model.getReferenceList(false, page, false);
		assertTrue(references.contains(siblingPage));
		assertFalse(references.contains(page));
	}

	@Test
	public void getReferencedByList() {
	}

}