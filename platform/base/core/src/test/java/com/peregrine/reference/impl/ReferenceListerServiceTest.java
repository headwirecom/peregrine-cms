package com.peregrine.reference.impl;

import com.peregrine.SlingResourcesTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
	public final class ReferenceListerServiceTest extends SlingResourcesTest {

	@Mock
	private ReferenceListerService.Configuration configuration;

	private final ReferenceListerService model = new ReferenceListerService();

	@Before
	public void setUp() {
		when(configuration.referencedByRoot()).thenReturn(new String[] { "/content/", null });
		when(configuration.referencePrefix()).thenReturn(new String[] { "/content", null });
		model.activate(configuration);
	}

	@Test
	public void modified() {
		model.modified(configuration);
	}

	@Test
	public void getReferenceList() {
	}

	@Test
	public void getReferencedByList() {
	}

}