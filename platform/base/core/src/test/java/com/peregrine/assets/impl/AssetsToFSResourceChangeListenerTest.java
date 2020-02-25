package com.peregrine.assets.impl;

import com.peregrine.commons.concurrent.Callback;
import com.peregrine.commons.concurrent.DeBouncer;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AssetsToFSResourceChangeListenerTest implements Callback<String> {

	private static final String PATH = "/content/dam/file";

	private final DeBouncer<String> deBouncer = new DeBouncer<>(this, 0);
	private final AssetsToFSResourceChangeListener model = new AssetsToFSResourceChangeListener(deBouncer);
	private final List<ResourceChange> changes = new LinkedList<>();

	private String value;

	@Before
	public void setUp() {
		changes.add(mockChange("/some other"));
		changes.add(mockChange(PATH));
	}

	private final ResourceChange mockChange(final String path) {
		final ResourceChange result = Mockito.mock(ResourceChange.class);
		when(result.getPath()).thenReturn(path);
		return result;
	}

	@Override
	public void call(final String arg) {
		value = arg;
	}

	@Test
	public synchronized void test() throws InterruptedException {
		model.onChange(changes);
		wait(100);
		assertEquals(PATH, value);
	}

}
