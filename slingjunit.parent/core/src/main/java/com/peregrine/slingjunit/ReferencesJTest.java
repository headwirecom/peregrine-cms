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

import com.peregrine.adaption.PerReplicable;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.replication.ReferenceLister;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static org.apache.jackrabbit.JcrConstants.JCR_LASTMODIFIED;
import static org.junit.Assert.*;
import static com.peregrine.admin.replication.ReplicationUtil.queryContainsStringUnderResource;

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

    private Resource contactRes;
    private Calendar timeBefore;
    private static String CONTACT_PATH = "/content/example/pages/contact";
    private static String IMAGE_PATH = "/content/example/assets/images/Stella.png";
    private static String IMAGE_COMPONENT = "/content/example/pages/contact/jcr:content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f/n8680c077-cc22-40d3-8989-d86d832a85d1";

    private static String TEMPLATE_PATH = "/content/example/templates/base";
    private static String LINKS_TO_PATH = "/content/example/pages/about";
    private static String TEXT_COMPONENT = "/content/example/pages/contact/jcr:content/nfceefd40-b802-4203-8147-9cb2b2c78c6b";


    @Test
    public void checkReferenceList(){
        List<Resource> resources = referenceLister.getReferenceList(false, contactRes, false);
        assertTrue(resources.stream().anyMatch(resource -> resource.getPath().equals(IMAGE_PATH)));
        assertTrue(resources.stream().anyMatch(resource -> resource.getPath().equals(TEMPLATE_PATH)));
    }

    @Test
    public void checkQueryResults(){
        Resource contactRes = resourceResolver.getResource(CONTACT_PATH);
        Iterator<Resource> results = queryContainsStringUnderResource(contactRes, "/content/");
        boolean hasImageRef = false;
        boolean hasPageLinkRef = false;
        boolean textContentFalseRef = false;
        while (results.hasNext()) {
            Resource result = results.next();
            if (result.getPath().equals(IMAGE_COMPONENT)){
                hasImageRef = true;
            } else if (result.getPath().equals(TEXT_COMPONENT)) {
                hasPageLinkRef = true;
            }
        }
        assertTrue(hasImageRef);
        assertTrue(hasPageLinkRef);
    }

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        contactRes = resourceResolver.getResource(CONTACT_PATH);
        timeBefore = Calendar.getInstance();
    }

    @After
    public void cleanup() {
        resourceResolver.close();
    }

}
