package com.peregrine.replication.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.render.RenderService;
import com.peregrine.replication.Replication;
import com.peregrine.replication.impl.LocalFileSystemReplicationService.Configuration;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class LocalFileSystemReplicationServiceTest extends SlingResourcesTest {

    private final BundleContext context = mock(BundleContext.class);
    private final Configuration config = mock(Configuration.class);
    private final RenderService renderService = mock(RenderService.class);

    private final LocalFileSystemReplicationService model = new LocalFileSystemReplicationService();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, NoSuchFieldException {
        final File root = temporaryFolder.newFolder("staticreplication");
        when(config.name()).thenReturn(root.getName());
        when(config.targetFolder()).thenReturn(root.getPath());
        when(config.exportExtensions()).thenReturn(new String[]{
                "data.json=per:Page|per:Template",
                "infinity.json=per:Object",
                "html=per:Page|per:Template",
                "*~raw=nt:file",
                "css=per:Site|per:FeLib",
                "js=per:Site|per:FeLib"
        });
        PrivateAccessor.setField(model, "renderService", renderService);
        model.activate(context, config);
    }

    @Test
    public void replicate() throws Replication.ReplicationException {
        final var result = model.replicate(
                resources.stream()
                .map(m -> (Resource)m)
                .collect(Collectors.toList())
        );
        assertTrue(result.contains(repo.getContent()));
        assertTrue(result.contains(parent));
        assertTrue(result.contains(page));
    }

}