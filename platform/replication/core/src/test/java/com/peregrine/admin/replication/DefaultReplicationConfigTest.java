package com.peregrine.admin.replication;

import com.peregrine.admin.replication.DefaultReplicationMapperService.DefaultReplicationConfig;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultReplicationConfigTest {

    @Test
    public void testIsHandled() throws Exception {
        DefaultReplicationConfig config = new DefaultReplicationConfig("test-rep", "/content/test/sub-folder", null);
        Resource handledResource = mock(Resource.class);
        when(handledResource.getPath()).thenReturn("/content/test/sub-folder/1");
        assertTrue("This resource: '" + handledResource.getPath() + "' failed to be handled", config.isHandled(handledResource));
        Resource edgeResource = mock(Resource.class);
        when(edgeResource.getPath()).thenReturn("/content/test/sub-folder");
        assertTrue("This resource: '" + edgeResource.getPath() + "' failed to be handled", config.isHandled(edgeResource));
        Resource unhandledResource = mock(Resource.class);
        when(unhandledResource.getPath()).thenReturn("/content/test/sub-folder-1");
        assertFalse("This resource: '" + unhandledResource.getPath() + "' was unexpectedly handled", config.isHandled(unhandledResource));
        Resource unhandledResource2 = mock(Resource.class);
        when(unhandledResource2.getPath()).thenReturn("/content/test/sub-folder-1/");
        assertFalse("This resource: '" + unhandledResource2.getPath() + "' was unexpectedly handled", config.isHandled(unhandledResource2));
        Resource unhandledResource3 = mock(Resource.class);
        when(unhandledResource3.getPath()).thenReturn("/content/test/sub-foldeA");
        assertFalse("This resource: '" + unhandledResource3.getPath() + "' was unexpectedly handled", config.isHandled(unhandledResource3));
    }
}