package com.peregrine.assets.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.assets.ResourceResolverFactoryProxy;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AssetsToFSResourceChangeJobConsumerTest extends SlingResourcesTest {

	private final String targetFolderRootPath = System.getProperty("user.dir") + "/target/" + getClass().getSimpleName();
	private final AssetsToFSResourceChangeJobConsumer model = new AssetsToFSResourceChangeJobConsumer();

	@Mock
	private JobManager jobManager;

	@Mock
	private ResourceResolverFactoryProxy resourceResolverFactory;

	@Mock
	private BundleContext context;

	@Mock
	private AssetsToFSResourceChangeJobConsumerConfig config;

	@Mock
	private Job job;

	@Before
	public void setUp() throws NoSuchFieldException, LoginException {
		PrivateAccessor.setField(model, "jobManager", jobManager);
		PrivateAccessor.setField(model, "resourceResolverFactory", resourceResolverFactory);
		when(resourceResolverFactory.getServiceResourceResolver()).thenReturn(resourceResolver);
		when(config.targetFolderRootPath()).thenReturn(targetFolderRootPath);
	}

	private void activate() {
		model.activate(context, config);
	}

	private JobResult process() {
		return model.process(job);
	}

	private void assertProcess(final JobResult result) {
		assertEquals(result, process());
	}

	@Test
	public void targetFolderRootPath_isNull() {
		when(config.targetFolderRootPath()).thenReturn(null);
		activate();
		assertProcess(JobResult.CANCEL);
	}

	@Test
	public void targetFolderRootPath_incorrectPath() {
		when(config.targetFolderRootPath()).thenReturn(">:<");
		activate();
		assertProcess(JobResult.CANCEL);
	}

}
