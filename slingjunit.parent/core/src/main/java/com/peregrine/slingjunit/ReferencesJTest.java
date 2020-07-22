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

package com.peregrine.slingjunit;

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.replication.ReferenceLister;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(SlingAnnotationsTestRunner.class)
public class ReferencesJTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @TestReference
    AdminResourceHandler resourceManagement;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver resourceResolver;

    @TestReference
    private ReferenceLister referenceLister;

    private static String CONTACT_PATH = "/content/example/pages/contact";
    private static String IMAGE_PATH = "/content/example/assets/images/Stella.png";
    private static String TEMPLATE_PATH = "/content/example/templates/base";
    private static String LINKS_TO_PATH = "/content/example/pages/about";

    @Test
    public void checkContactPageReferences() {
        Resource contactRes = resourceResolver.getResource(CONTACT_PATH);
        List<Resource> refs = referenceLister.getReferenceList(false, contactRes, false );
        assertThat(refs, not(IsEmptyCollection.empty()));
        assertTrue(refs.stream().anyMatch(resource -> resource.getPath().equals(IMAGE_PATH)));
        assertTrue(refs.stream().anyMatch(resource -> resource.getPath().equals(TEMPLATE_PATH)));
        assertTrue(refs.stream().anyMatch(resource -> resource.getPath().equals(LINKS_TO_PATH)));
    }

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
    }

    @After
    public void cleanup() {
        resourceResolver.close();
    }

}
