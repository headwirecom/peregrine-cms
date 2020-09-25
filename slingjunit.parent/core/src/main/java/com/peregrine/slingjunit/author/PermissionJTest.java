/*-
 * #%L
 * slingtests admin-base core
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 * Contributed by Cris Rockwell, University of Michigan
 */

package com.peregrine.slingjunit.author;

import com.peregrine.admin.resource.AdminResourceHandler;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.slingjunit.VersionsJTest.EXAMPLE_SITE_ROOT;
import static org.junit.Assert.*;

@RunWith(SlingAnnotationsTestRunner.class)
public class PermissionJTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @TestReference
    AdminResourceHandler resourceManagement;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver resourceResolver;


    @Test
    public void runAsAdmin() {
        try {
            resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            assertTrue(resourceManagement.hasPermission(resourceResolver, "ALL", EXAMPLE_SITE_ROOT));
            assertTrue(resourceManagement.hasPermission(resourceResolver, "VERSION_MANAGEMENT", EXAMPLE_SITE_ROOT));
        } catch (LoginException e) {
            fail("No login");
        }
    }

    @Test
    public void runAsAnonymous() {
        try {
            resourceResolver = resolverFactory.getResourceResolver(null);
            assertEquals("anonymous", resourceResolver.getUserID());
            assertFalse(resourceManagement.hasPermission(resourceResolver, "ALL" , EXAMPLE_SITE_ROOT));
            assertFalse(resourceManagement.hasPermission(resourceResolver, "READ_NODE,READ_PROPERTY", EXAMPLE_SITE_ROOT));
        } catch (LoginException e) {
            fail("No login");
        }
    }

    @After
    public void cleanup(){
        resourceResolver.close();
        resourceResolver = null;
    }
}
