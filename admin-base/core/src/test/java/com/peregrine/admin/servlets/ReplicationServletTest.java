package com.peregrine.admin.servlets;

import com.peregrine.SlingServletTest;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.replication.PerReplicable;
import com.peregrine.replication.Replication;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static com.peregrine.admin.servlets.ReplicationServlet.DEACTIVATE;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReplicationServletTest extends SlingServletTest {

    private final ReplicationServlet servlet = new ReplicationServlet();

    private final AdminResourceHandler resourceManagement = mock(AdminResourceHandler.class);
    private final Replication replication = mock(Replication.class);
    private final PerReplicable replicable = mock(PerReplicable.class);

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(servlet, "resourceManagement", resourceManagement);
        jcrContent.addAdapter(replicable);
        when(replicable.getMainResource()).thenReturn(jcrContent);
    }

    @Test
    public void deactivate() throws IOException, Replication.ReplicationException {
        request.putParameter(DEACTIVATE, true);
        final AbstractBaseServlet.Request request = new AbstractBaseServlet.Request(this.request, response);
        when(replication.deactivate(page)).thenReturn(Collections.singletonList(jcrContent));
        final String response = servlet.performReplication(replication, request, page, resourceResolver).getContent();
        assertTrue(response.contains(jcrContent.getPath()));
    }

}