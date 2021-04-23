package com.peregrine.replication.impl;

import com.peregrine.SlingResourcesTest;
import org.junit.Test;

import java.util.Calendar;

import static com.peregrine.commons.util.PerConstants.*;
import static org.junit.Assert.*;

public final class PerReplicableImplTest extends SlingResourcesTest {

    private final PerReplicableImpl model = new PerReplicableImpl(page);

    @Test
    public void getMainResource() {
        assertEquals(jcrContent, model.getMainResource());
    }

    @Test
    public void isReplicated() {
        assertFalse(model.isReplicated());
        jcrContent.putProperty(PER_REPLICATION_REF, "ref");
        assertTrue(model.isReplicated());
    }

    @Test
    public void getReplicated() {
        assertNull(model.getReplicated());
        final Calendar time = Calendar.getInstance();
        jcrContent.putProperty(PER_REPLICATED, time);
        assertEquals(time, model.getReplicated());
    }

    @Test
    public void isStale() {
        assertFalse(model.isStale());
        final Calendar replicated = Calendar.getInstance();
        jcrContent.putProperty(PER_REPLICATED, replicated);
        final Calendar modified = Calendar.getInstance();
        modified.setTimeInMillis(replicated.getTimeInMillis() + 1);
        jcrContent.putProperty(JCR_LAST_MODIFIED, modified);
        assertTrue(model.isStale());
    }

    @Test
    public void getReplicationRef() {
        assertNull(model.getReplicationRef());
        jcrContent.putProperty(PER_REPLICATION_REF, "ref");
        assertNotNull(model.getReplicationRef());
    }

    @Test
    public void getLastReplicationAction() {
        assertNull(model.getLastReplicationAction());
        jcrContent.putProperty(PER_REPLICATION_LAST_ACTION, "activate");
        assertNotNull(model.getLastReplicationAction());
    }
}