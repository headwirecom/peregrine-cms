package com.peregrine.admin.servlets;

import com.peregrine.replication.Replication;
import org.junit.Test;

import java.io.IOException;

import static com.peregrine.admin.servlets.ReplicationServlet.DEACTIVATE;

public final class ReplicationServletTest extends ReplicationServletTestBase {

    public ReplicationServletTest() throws NoSuchFieldException, Replication.ReplicationException {
        super(new ReplicationServlet());
    }

    @Test
    public void performDeactivation() throws IOException {
        request.putParameter(DEACTIVATE, true);
        performReplicationResponseContains(jcrContent);
    }

    @Test
    public void performActivation() throws IOException {
        performReplicationResponseContains(jcrContent);
    }

}
