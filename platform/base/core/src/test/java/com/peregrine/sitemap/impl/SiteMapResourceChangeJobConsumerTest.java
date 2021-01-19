package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.SiteMapStructureCache;
import com.peregrine.sitemap.SiteStructureTestBase;
import com.peregrine.sitemap.VersioningResourceResolverFactory;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Set;

import static org.apache.jackrabbit.JcrConstants.JCR_FROZENPRIMARYTYPE;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapResourceChangeJobConsumerTest extends SiteStructureTestBase {

    private final String[] primaryTypes = { "per:X", "per:Y", "per:Z" };
    private final SiteMapResourceChangeJobConsumer model = new SiteMapResourceChangeJobConsumer();
    private final Set<String> initialPaths = new HashSet<>();

    @Mock
    private SiteMapStructureCache cache;

    @Mock
    private VersioningResourceResolverFactory resourceResolverFactory;

    @Mock
    private SiteMapResourceChangeJobConsumerConfig config;

    @Mock
    private Job job;

    @Before
    public void setUp() throws NoSuchFieldException, LoginException, RepositoryException {
        PrivateAccessor.setField(model, "resourceResolverFactory", resourceResolverFactory);
        PrivateAccessor.setField(model, "cache", cache);
        when(config.primaryTypes()).thenReturn(primaryTypes);
        model.activate(config);
        when(job.getProperty(SiteMapResourceChangeJobConsumer.PN_PATHS, Set.class)).thenReturn(initialPaths);
        when(resourceResolverFactory.createResourceResolver()).thenReturn(versioningResolver);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void handleLoginException() throws LoginException {
        when(resourceResolverFactory.createResourceResolver()).thenThrow(LoginException.class);
        assertEquals(JobConsumer.JobResult.CANCEL, model.process(job));
    }

    @Test
    public void process() {
        resource.putProperty(JCR_FROZENPRIMARYTYPE, "per:X");
        initialPaths.add(resource.getPath());
        page.putProperty(JCR_FROZENPRIMARYTYPE, "per:Page");
        initialPaths.add(page.getPath());
        parent.putProperty(JCR_FROZENPRIMARYTYPE, "per:Y");
        initialPaths.add(parent.getPath());
        final Set<Object> rebuilt = new HashSet<>();
        doAnswer(invocation -> {
            rebuilt.add(invocation.getArguments()[0]);
            return null;
        }).when(cache).rebuild(anyString());
        assertEquals(JobConsumer.JobResult.OK, model.process(job));
        assertTrue(rebuilt.contains(resource.getPath()));
        assertFalse(rebuilt.contains(page.getPath()));
    }

}
