package com.peregrine.commons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TextUtilsTest {

    @Test
    public void createsStaticReplicationPath(){
        String dir = System.getProperty("user.dir");
        System.getProperties().setProperty("sling.home", dir);
        String staticReplicationPath = TextUtils.replacePlaceholders("${sling.home}/staticreplication", (String) -> null);
        assertNotNull(staticReplicationPath);
        assertTrue(staticReplicationPath.endsWith("/staticreplication"));
        assertEquals(dir+"/staticreplication", staticReplicationPath);
    }
}
