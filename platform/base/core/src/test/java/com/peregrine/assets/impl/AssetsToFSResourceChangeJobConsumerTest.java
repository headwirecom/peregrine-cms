package com.peregrine.assets.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.assets.ResourceResolverFactoryProxy;
import com.peregrine.commons.ResourceUtils;
import junitx.util.PrivateAccessor;
import org.apache.commons.io.FileUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static com.peregrine.assets.impl.AssetsToFSResourceChangeJobConsumer.PN_PATH;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AssetsToFSResourceChangeJobConsumerTest extends SlingResourcesTest {

	private static final String ROOT_PATH = "/content/assets";
	private static final String[] NULL_PATHS_ARRAY = null;

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
	private ServiceRegistration<ResourceChangeListener> resourceChangeListener;

	@Mock
	private Job job;

	@Before
	public void setUp() throws NoSuchFieldException, LoginException {
		PrivateAccessor.setField(model, "jobManager", jobManager);
		PrivateAccessor.setField(model, "resourceResolverFactory", resourceResolverFactory);
		when(resourceResolverFactory.getServiceResourceResolver()).thenReturn(resourceResolver);
		when(config.targetFolderRootPath()).thenReturn(targetFolderRootPath);
		when(config.sourceAssetsRootPaths()).thenReturn(new String[] { ROOT_PATH });
		when(context.registerService(eq(ResourceChangeListener.class), any(AssetsToFSResourceChangeListener.class), any()))
				.thenReturn(resourceChangeListener);
		final InputStream is = getClass().getResourceAsStream(EMPTY);
		when(resource.adaptTo(InputStream.class)).thenReturn(is);
		resource.setPrimaryType(NT_FILE);
	}

	@After
	public void tearDown() throws IOException {
		model.deactivate();
		final File root = new File(targetFolderRootPath);
		if (root.exists()) {
			FileUtils.cleanDirectory(root);
			root.delete();
		}
	}

	private void activate() {
		model.activate(context, config);
	}

	private void activate(final String... paths) {
		when(config.sourceAssetsRootPaths()).thenReturn(paths);
		activate();
	}

	private JobResult process() {
		return model.process(job);
	}

	private void assertProcess(final JobResult result) {
		assertEquals(result, process());
	}

	private void assertProcess(final String path, final JobResult result) {
		when(job.getProperty(PN_PATH, String.class)).thenReturn(path);
		assertProcess(result);
	}

	@Test
	public void targetFolderRootPath_isNull() {
		when(config.targetFolderRootPath()).thenReturn(null);
		activate(NULL_PATHS_ARRAY);
		assertProcess(JobResult.CANCEL);
	}

	@Test
	public void targetFolderRootPath_incorrectPath() {
		when(config.targetFolderRootPath()).thenReturn(">:<");
		activate(NULL_PATHS_ARRAY);
		assertProcess(JobResult.CANCEL);
	}

	@Test
	public void targetFolderRootPath_doesNotExist() {
		final File root = new File(targetFolderRootPath);
		assertFalse(root.exists());
		activate(NULL_PATHS_ARRAY);
		assertProcess(JobResult.CANCEL);
		assertTrue(root.exists());
	}

	@Test
	public void targetFolderRootPath_exists() throws IOException {
		final File root = new File(targetFolderRootPath);
		FileUtils.forceMkdir(root);
		assertTrue(root.exists());
		activate(NULL_PATHS_ARRAY);
		assertProcess(JobResult.CANCEL);
		assertTrue(root.exists());
	}

	@Test
	public void registerResourceChangeListener_emptyArray() {
		activate(new String[0]);
		assertProcess(JobResult.CANCEL);
	}

	@Test
	public void registerResourceChangeListener() {
		activate();
		assertProcess(JobResult.OK);
	}

	@Test
	public void findSuperElement() {
		assertEquals("/x/a/w", model.findSuperElement("/x/a/w", asList()));
		assertEquals("/x/a", model.findSuperElement("/x/a/w", asList("/x/b", "/x/a/s", "/x/a")));
		assertEquals("/x", model.findSuperElement("/x/a/w", asList("/x/b", "/x/a/s", "/x")));
	}

	@Test
	public void findSubElements() {
		assertFalse(model.findSubElements("/x/a/w", asList()).contains("/x/a/w"));

		Set<String> subElements = model.findSubElements("/x/a/w", asList("/x/b", "/x/a/s", "/x/a"));
		assertTrue(subElements.contains("/x/a/w"));
		assertTrue(subElements.contains("/x/a/s"));
		assertFalse(subElements.contains("/x/a"));
		assertFalse(subElements.contains("/x/b"));

		subElements = model.findSubElements("/x/a/w", asList("/x/b", "/x/a/s", "/x"));
		assertTrue(subElements.contains("/x/a/w"));
		assertTrue(subElements.contains("/x/b"));
		assertTrue(subElements.contains("/x/a/s"));
		assertFalse(subElements.contains("/x"));
	}

	@Test
	public void updateFiles() {
		activate();
		final String pagePath = page.getPath();
		final String resourcePath = resource.getPath();
		final File file = new File(targetFolderRootPath + ResourceUtils.jcrPathToFilePath(resourcePath));

		assertProcess(pagePath, JobResult.OK);
		assertTrue(file.exists());

		assertProcess(pagePath + "/none", JobResult.OK);
		assertTrue(file.exists());

		assertProcess(pagePath, JobResult.OK);
		assertTrue(file.exists());
		when(resourceResolver.getResource(pagePath)).thenReturn(null);
		assertProcess(pagePath, JobResult.OK);
		assertFalse(file.exists());

		when(resourceResolver.getResource(pagePath)).thenReturn(page);
		assertProcess(pagePath, JobResult.OK);
		assertTrue(file.exists());
		when(resourceResolver.getResource(resourcePath)).thenReturn(null);
		assertProcess(resourcePath, JobResult.OK);
		assertFalse(file.exists());
	}

	@Test
	public void deleteMissingAncestorFolder_ancestorIsNull() {
		activate();
		when(resourceResolver.getResource(SLASH)).thenReturn(null);
		assertProcess(SLASH, JobResult.OK);
	}

}
