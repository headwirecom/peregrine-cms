package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.event.jobs.JobManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapResourceChangeListenerTest extends SlingResourcesTest {

    private final SiteMapResourceChangeListener model = new SiteMapResourceChangeListener();
    private final List<ResourceChange> changes = new LinkedList<>();
    private final Map<String, Object> props = new HashMap<>();

    @Mock
    private JobManager jobManager;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "jobManager", jobManager);
        addChange(resource.getPath());
        addChange(jcrContent.getPath());
        addChange(page.getPath());
    }

    private void addChange(final String path) {
        final ResourceChange change = mock(ResourceChange.class);
        when(change.getPath()).thenReturn(path);
        changes.add(change);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void onChange() {
        when(jobManager.addJob(any(), any())).thenAnswer(invocation -> {
            props.putAll((Map<String, Object>) invocation.getArguments()[1]);
            return null;
        });
        model.onChange(changes);
        assertTrue(props.containsKey(SiteMapResourceChangeJobConsumer.PN_PATHS));
        final Object pathsObj = props.get(SiteMapResourceChangeJobConsumer.PN_PATHS);
        assertTrue(pathsObj instanceof Set);
        final Set<String> paths = (Set<String>) pathsObj;
        assertTrue(paths.contains(resource.getPath()));
        assertTrue(paths.contains(jcrContent.getPath()));
        assertTrue(paths.contains(page.getPath()));
    }

}
