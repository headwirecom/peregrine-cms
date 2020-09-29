package com.peregrine.slingjunit;

import com.peregrine.replication.Replication;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.api.resource.Resource;


import java.io.IOException;
import java.util.Calendar;

import static java.util.Objects.isNull;
import static org.junit.Assert.*;

public abstract class ReplicationTestBase  {

    private static String PUBLISH_DOMAIN = "http://localhost:8180";


    protected Calendar beforeTime;
    protected void setPublishDomain(String publishDomain) {
        this.publishDomain = publishDomain;
    }

    private String publishDomain;

    protected void assertPublishedStatus(String path, int status) {
        String domain = ! isNull(this.publishDomain) ? this.publishDomain : PUBLISH_DOMAIN;
        HttpUriRequest request = new HttpGet( domain + path);

        int retries = 10;
        int attemptStatus = 0;
        try {
            while (attemptStatus != status && retries-- > 0) {
                Thread.sleep(1000);
                HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );
                attemptStatus = httpResponse.getStatusLine().getStatusCode();
            }
            assertEquals(status, attemptStatus );
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
    }


    protected void deactivateResource(String path, Resource resource, Replication replication) {
        try {
            replication.deactivate(resource);
        } catch (Replication.ReplicationException e) {
            fail(e.getMessage());
        }
    }
}
