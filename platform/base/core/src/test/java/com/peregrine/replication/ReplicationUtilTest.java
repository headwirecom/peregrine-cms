package com.peregrine.replication;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageContentMock;
import com.peregrine.mock.PageMock;
import com.peregrine.replication.impl.PerReplicableImpl;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.mock.MockTools.setParentChildRelationships;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public final class ReplicationUtilTest extends SlingResourcesTest {

    private static final String ADMIN = "admin";
    private static final String TARGET_PATH = "/live/page";

    private final PageMock target = new PageMock();
    private final PageContentMock targetContent = target.getContent();

    @Before
    public void setUp() {
        ReplicationUtil.setReplicationPrimaryNodeTypes(Arrays.asList(
                PAGE_PRIMARY_TYPE,
                PAGE_CONTENT_TYPE
        ));
        when(resourceResolver.getUserID()).thenReturn(ADMIN);
        target.setPath(TARGET_PATH);
        setParentChildRelationships(repo.getLive(), target);
    }

    private void assertNoProps() {
        assertNull(jcrContent.getProperty(PER_REPLICATED_BY));
        final PerReplicable replicable = new PerReplicableImpl(page);
        assertNull(replicable.getReplicated());
        assertNull(replicable.getLastReplicationAction());
        assertNull(replicable.getReplicationRef());
    }

    private void assertActivated() {
        assertEquals(ADMIN, jcrContent.getProperty(PER_REPLICATED_BY));
        final PerReplicable replicable = new PerReplicableImpl(page);
        assertNotNull(replicable.getReplicated());
        assertEquals(ACTIVATED, replicable.getLastReplicationAction());
        assertEquals(TARGET_PATH, replicable.getReplicationRef());
    }

    private void assertDeactivated() {
        assertEquals(ADMIN, jcrContent.getProperty(PER_REPLICATED_BY));
        final PerReplicable replicable = new PerReplicableImpl(page);
        assertNotNull(replicable.getReplicated());
        assertEquals(DEACTIVATED, replicable.getLastReplicationAction());
        assertNull(replicable.getReplicationRef());
    }

    @Test
    public void markAsActivated_path() {
        ReplicationUtil.markAsActivated(jcrContent, EMPTY);
        assertNoProps();
        ReplicationUtil.markAsActivated(jcrContent, TARGET_PATH);
        assertActivated();
    }

    @Test
    public void markAsActivated_page() {
        ReplicationUtil.markAsActivated(jcrContent, (Resource) null);
        assertNoProps();
        ReplicationUtil.markAsActivated(jcrContent, targetContent);
        assertActivated();
    }

    @Test
    public void markAsDeactivated() {
        ReplicationUtil.markAsDeactivated(jcrContent);
        assertDeactivated();
    }

    @Test
    public void updateReplicationProperties_deactivate() {
        ReplicationUtil.updateReplicationProperties(null, null, null);
        assertNoProps();
        ReplicationUtil.updateReplicationProperties(jcrContent, null, null);
        assertDeactivated();
        ReplicationUtil.updateReplicationProperties(jcrContent, TARGET_PATH, null);
        assertActivated();
        ReplicationUtil.updateReplicationProperties(jcrContent, null, targetContent);
        assertActivated();
    }

    @Test
    public void updateReplicationProperties_path() {
        ReplicationUtil.updateReplicationProperties(jcrContent, TARGET_PATH, null);
        assertActivated();
    }

    @Test
    public void updateReplicationProperties_page() {
        ReplicationUtil.updateReplicationProperties(jcrContent, null, targetContent);
        assertActivated();
    }

}