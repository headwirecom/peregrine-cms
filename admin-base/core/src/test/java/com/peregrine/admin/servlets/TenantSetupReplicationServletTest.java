package com.peregrine.admin.servlets;

import com.peregrine.replication.Replication;
import org.junit.Test;

import java.io.IOException;

public class TenantSetupReplicationServletTest extends ReplicationServletTestBase {

    public TenantSetupReplicationServletTest() throws NoSuchFieldException, Replication.ReplicationException {
        super(new TenantSetupReplicationServlet());
    }

    @Test
    public void notSite() throws IOException, Replication.ReplicationException {
        performReplicationResponseContains("message");
    }

}