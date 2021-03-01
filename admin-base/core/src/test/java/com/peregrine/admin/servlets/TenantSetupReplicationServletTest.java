package com.peregrine.admin.servlets;

import com.peregrine.mock.SiteMock;
import com.peregrine.replication.Replication;
import com.peregrine.sitemap.SiteMapFilesCache;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class TenantSetupReplicationServletTest extends ReplicationServletTestBase {

    private final SiteMapFilesCache siteMapFilesCache = mock(SiteMapFilesCache.class);
    private final SiteMock site = new SiteMock(repo, "site");

    public TenantSetupReplicationServletTest() throws NoSuchFieldException, Replication.ReplicationException {
        super(new TenantSetupReplicationServlet());
        setField("siteMapFilesCache", siteMapFilesCache);
    }

    @Test
    public void notSite() throws IOException {
        performReplicationResponseContains("message");
    }

    @Test
    public void site() throws IOException {
        performReplicationResponseContains(site,site.getPath());
    }

}
