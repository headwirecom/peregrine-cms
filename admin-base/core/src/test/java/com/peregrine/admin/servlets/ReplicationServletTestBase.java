package com.peregrine.admin.servlets;

import com.peregrine.SlingServletTest;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.mock.PageMock;
import com.peregrine.replication.PerReplicable;
import com.peregrine.replication.Replication;
import com.peregrine.replication.ReplicationsContainerWithDefault;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.Resource;
import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.peregrine.commons.util.PerConstants.PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReplicationServletTestBase extends SlingServletTest {

    private final ReplicationServletBase servlet;

    private final AdminResourceHandler resourceManagement = mock(AdminResourceHandler.class);
    private final ReplicationsContainerWithDefault replications = mock(ReplicationsContainerWithDefault.class);

    private final Replication replication = mock(Replication.class);
    private final PerReplicable replicable = mock(PerReplicable.class);

    public ReplicationServletTestBase(final ReplicationServletBase servlet)
            throws NoSuchFieldException, Replication.ReplicationException
    {
        this.servlet = servlet;
        setField("replications", replications);
        setField("resourceManagement", resourceManagement);

        when(replications.getOrDefault(anyString())).thenReturn(replication);

        when(replication.prepare(any())).thenAnswer(i -> i.getArguments()[0]);
        when(replication.replicate(any())).thenAnswer(i -> i.getArguments()[0]);
        when(replication.deactivate(any(PageMock.class))).thenAnswer(
                i -> Collections.singletonList(
                        ((PageMock)i.getArguments()[0]).getContent()
                )
        );

        page.addAdapter(replicable);
        jcrContent.addAdapter(replicable);
        when(replicable.getMainResource()).thenReturn(jcrContent);
    }

    protected void setField(final String name, final Object value) throws NoSuchFieldException {
        PrivateAccessor.setField(servlet, name, value);
    }

    protected void performReplicationResponseContains(final PageMock page, final String... substrings) throws IOException {
        request.putParameter(PATH, page.getPath());
        final AbstractBaseServlet.Request request = new AbstractBaseServlet.Request(this.request, response);
        final String response = servlet.handleRequest(request).getContent();
        Arrays.stream(substrings)
                .map(response::contains)
                .forEach(Assert::assertTrue);
    }

    protected void performReplicationResponseContains(final String... substrings) throws IOException {
        performReplicationResponseContains(page, substrings);
    }

    protected void performReplicationResponseContains(final Resource... resources) throws IOException {
        performReplicationResponseContains(Arrays.stream(resources)
                .map(Resource::getPath)
                .toArray(String[]::new)
        );
    }

}