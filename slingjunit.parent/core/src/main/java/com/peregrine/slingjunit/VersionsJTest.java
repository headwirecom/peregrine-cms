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

import com.google.common.collect.Iterators;
import com.peregrine.admin.models.PageModel;
import com.peregrine.admin.models.Recyclable;
import com.peregrine.admin.resource.AdminResourceHandlerService;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.replication.ReferenceLister;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.peregrine.admin.resource.AdminResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import java.util.Calendar;
import java.util.List;


@RunWith(SlingAnnotationsTestRunner.class)
public class VersionsJTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @TestReference
    private ResourceResolverFactory resolverFactory;

    @TestReference
    private ReferenceLister referenceLister;

    @TestReference
    AdminResourceHandler resourceManagement;

    private ResourceResolver resourceResolver;
    private Session jcrSession;

    static final public String EXAMPLE_SITE_ROOT = "/content/example";
    static final String EXAMPLE_PAGES = EXAMPLE_SITE_ROOT +"/pages";
    static final String EXAMPLE_INDEX = "/pages/index";
    static final String EXAMPLE_ABOUT = "/pages/about";
    static final String EXAMPLE_ASSET = "/assets/images/peregrine-logo.png";
    static final String [] EXAMPLE_PAGE_PATHS = { "/index", "/about", "/services",
            "/contact", "/services/jcr:content/content/row/col1/text2" };

    // page objects
    private Resource indexRes;
    private PageModel indexPage;
    private Node indexNode;
    private Resource aboutRes;
    private Node aboutNode;
    private Resource pagesRes;
    private Node pagesNode;
    // asset objects
    private Resource assetRes;
    private Node assetNode;
    // version managers
    private VersionManager vmPage;
    private VersionManager vmAsset;

    // path to check
    String yearMonthFolders;
    String pathPrefix;

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        jcrSession = resourceResolver.adaptTo(Session.class);
        // get a per:Page
        indexRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+ EXAMPLE_INDEX);
        indexPage = indexRes.adaptTo(PageModel.class);
        indexNode = indexRes.adaptTo(Node.class);
        // get a per:Asset (just a resource?)
        assetRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_ASSET);
        assetNode = assetRes.adaptTo(Node.class);
        // version managers
        vmPage = indexNode.getSession().getWorkspace().getVersionManager();
        vmAsset = assetNode.getSession().getWorkspace().getVersionManager();

        aboutRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+ EXAMPLE_ABOUT);
        aboutNode = aboutRes.adaptTo(Node.class);

        pagesRes = resourceResolver.getResource(EXAMPLE_PAGES);
        pagesNode = pagesRes.adaptTo(Node.class);

        yearMonthFolders = AdminResourceHandlerService.RECYCLABLE_PATH_DATE_FORMAT.format(Calendar.getInstance().getTime());
        pathPrefix = PerConstants.RECYCLE_BIN_PATH + EXAMPLE_SITE_ROOT + PerConstants.SLASH + yearMonthFolders;
    }

    @Test
    public void resourcesNotNull() {
        Assert.assertNotNull(resolverFactory);
        Assert.assertNotNull(resourceResolver);
        Assert.assertNotNull(jcrSession);
        Assert.assertNotNull(resourceManagement);
        Assert.assertNotNull(referenceLister);
        Assert.assertNotNull(indexRes);
        Assert.assertNotNull(assetRes);
        Assert.assertNotNull(indexPage);
        Assert.assertNotNull(pagesRes);
        Assert.assertNotNull(pagesNode);
    }

    @Test
    public void pageCanBeVersionable() {
        try {
            if (indexNode.canAddMixin("mix:versionable")) {
                Assert.assertTrue(indexNode.canAddMixin("mix:versionable"));
                logger.info("page can become versionable but currently is not");
                return;
            }
            if (vmPage.getVersionHistory(indexRes.getPath()) == null) {
                logger.info("page is versionable and has a version history");
                return;
            } else {
                NodeType[] nodeTypes = indexNode.getMixinNodeTypes();
                for (NodeType nt : nodeTypes) {
                    if (nt.isNodeType("mix:versionable")){
                        logger.info("page is versionable but has no history");
                        return;
                    }
                }
                Assert.fail("not versionable; mixin type mix:versionable cant be added");
            }
        } catch (RepositoryException e) {
            logger.error("RepositoryException", e);
        }
        Assert.fail("page is not versionable");
    }

    @Test
    public void assetResourceCanBeVersionable() {
        try {
            Assert.assertTrue(assetNode.canAddMixin("mix:versionable"));
        } catch (RepositoryException e) {
            Assert.fail("no type mix:versionable");
        }
    }

    @Test
    public void makeFirstPageVersion() {
        try {
            Version version = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            Assert.assertNotNull(version);
            VersionHistory vhPage = vmPage.getVersionHistory(indexNode.getPath());
            // check that the number of versions is 2 (root and one version)
            int size = Iterators.size(vhPage.getAllLinearVersions());
            Assert.assertEquals(2, size);
            Version rootVersion = vhPage.getRootVersion();
            Version firstVersion = rootVersion.getLinearSuccessor();
            Assert.assertEquals("1.0", firstVersion.getName());
            Assert.assertEquals(firstVersion.getName(), version.getName());
            Assert.assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
        } catch (Exception e) {
            Assert.fail("could not create version");
        }
    }

    @Test
    public void make2ndPageVersion() {
        try {
//            First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            Assert.assertNotNull(version);
            Assert.assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
//            Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            Assert.assertNotNull(version2);
            // check that the number of versions is 2 (root and two versions)
            VersionHistory vhPage2 = vmPage.getVersionHistory(indexNode.getPath());
            int size3 = Iterators.size(vhPage2.getAllLinearVersions());
            Assert.assertEquals(3, size3);
            Assert.assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
        } catch (Exception e) {
            Assert.fail("could not create version");
        }
    }

    @Test
    public void restoreFirstPageVersion() {
        try {
            // First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            // Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            Assert.assertNotNull(version2);
            // check that the current version has a name = 1.1
            Assert.assertEquals("1.1" , vmPage.getBaseVersion(indexPage.getPath()).getName());
            Assert.assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
            // Restore the first version
            String frozenNodepath = version.getPath();
            Node frozenNode = jcrSession.getNode(frozenNodepath);
            Version versionToRestore = (Version) frozenNode;
            Assert.assertNotNull(versionToRestore);
            Resource restoredResource = resourceManagement.restoreVersion(resourceResolver, indexPage.getPath(), frozenNodepath, true);
            Assert.assertNotNull(restoredResource);
            Assert.assertEquals("1.0" , vmPage.getBaseVersion(restoredResource.getPath()).getName());
            // check that the test page is not left in a "checked out" or locked state
            Assert.assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
        } catch (Exception e) {
            Assert.fail("could not create version");
        }
    }

    @Test
    public void resolveSiteHome() {
        // get home from a page
        String home = resourceManagement.getSiteHomePath(resourceResolver, indexRes);
        Assert.assertNotNull(home);
        Assert.assertEquals("/content/example", home);
        // get home from an asset
        home = resourceManagement.getSiteHomePath(resourceResolver, assetRes);
        Assert.assertNotNull(home);
        Assert.assertEquals("/content/example", home);
    }

    @Test
    public void createRecyclable(){
        try {
            Recyclable recyclable = resourceManagement.createRecyclable(resourceResolver, indexRes);
            Assert.assertNotNull(recyclable);
            Assert.assertTrue(recyclable.getResource().getPath().startsWith(pathPrefix));
            Assert.assertTrue(recyclable.getFrozenNodePath().startsWith("/jcr:system/jcr:versionStorage/"));
            Assert.assertTrue(recyclable.getResource().getName().matches("\\d+"));
        } catch (AdminResourceHandler.ManagementException e) {
            Assert.fail("execption while creating recyclable");
        }
    }

    @Test
    public void findAndRestoreRecyclable (){
        try {
            String aboutPagePath = aboutRes.getPath();
            // Delete the page
            resourceManagement.deleteResource(resourceResolver, aboutPagePath, PerConstants.PAGE_PRIMARY_TYPE);
            resourceResolver.commit();
            aboutRes = resourceResolver.getResource(aboutPagePath);
            Assert.assertNull(aboutRes);
            List<Recyclable> recyclables = resourceManagement.getRecyclables(resourceResolver, pathPrefix);
            Assert.assertNotNull(recyclables);
            resourceManagement.recycleDeleted(resourceResolver, recyclables.get(0), true );
            Assert.assertTrue(vmPage.isCheckedOut(aboutPagePath));
            resourceResolver.refresh();
            resourceResolver.commit();
        } catch (Exception e) {
            Assert.fail("could not find and restore recyclable");
        }
    }

    @Test
    public void deleteAllPagesAndRestore () {
        Assert.assertNotNull(pagesRes);
        Assert.assertEquals(EXAMPLE_PAGES, pagesRes.getPath());
        try {
            Recyclable createdRecyclable = resourceManagement.createRecyclable(resourceResolver, pagesRes);
            Assert.assertNotNull(createdRecyclable);
            resourceResolver.delete(pagesRes);
            resourceResolver.refresh();
            resourceResolver.commit();
            // all gone
            for (String path : EXAMPLE_PAGE_PATHS) {
                Assert.assertNull(resourceResolver.getResource(EXAMPLE_PAGES + path));
            }

            List<Recyclable> foundRecyclable = resourceManagement.getRecyclables(resourceResolver, pathPrefix);
            Assert.assertNotNull(foundRecyclable);
            resourceManagement.recycleDeleted(resourceResolver,foundRecyclable.get(0), false);
            // all back
            for (String path : EXAMPLE_PAGE_PATHS) {
                Assert.assertNotNull(resourceResolver.getResource(EXAMPLE_PAGES + path));
            }
        } catch (AdminResourceHandler.ManagementException e) {
            Assert.fail("Could create recyclable for pages: " + EXAMPLE_PAGES);
        } catch (Exception e) {
            Assert.fail("Could not delete resource: " + EXAMPLE_PAGES);
        }
    }

    @Test
    public void restoreDeletedPage() {
        try {
            // Create a Version
            Version version = resourceManagement.createVersion(this.resourceResolver, aboutNode.getPath());
            VersionHistory vhPage = vmPage.getVersionHistory(aboutNode.getPath());
            Assert.assertEquals(2, Iterators.size(vhPage.getAllLinearVersions()));
            Assert.assertNotNull(version);
            String idBefore = aboutNode.getIdentifier();
            List<Resource> beforeRefs = referenceLister.getReferenceList(true, aboutRes, true);

            // Record paths to restore
            String versionPath = version.getPath();
            String resourcePath = aboutNode.getPath();
            // Delete the page
            resourceResolver.delete(aboutRes);
            aboutRes = resourceResolver.getResource(resourcePath);
            Assert.assertNull(aboutRes);
            resourceResolver.commit();

            // Restore at the recorded version
            Resource restoredResource = resourceManagement.restoreDeleted(resourceResolver, resourcePath, versionPath, true);
            Assert.assertNotNull(restoredResource);
            Assert.assertNotNull(resourceResolver.getResource(resourcePath));
            Assert.assertEquals("1.0" , vmPage.getBaseVersion(restoredResource.getPath()).getName());
            String idAfter = restoredResource.adaptTo(Node.class).getIdentifier();
            List<Resource> afterRefs = referenceLister.getReferenceList(true, restoredResource, true);

            // jcr:uuid not guaranteed the same after restoring deleted
            Assert.assertEquals(idAfter, idBefore);

            // All references are the same
            Assert.assertEquals(afterRefs.size(), beforeRefs.size());
            for (int i=0; i < beforeRefs.size(); i++) {
                boolean contained = false;
                for (int j=0; j < afterRefs.size(); j++) {
                    if (beforeRefs.get(i).getPath().equals(afterRefs.get(j).getPath())) {
                        contained = true;
                        break;
                    }
                }
                Assert.assertTrue(contained);
            }

            // check that the test page is not left in a "checked out" or locked state
            Assert.assertTrue(vmPage.isCheckedOut(restoredResource.getPath()));
            // Clean up
            aboutRes = restoredResource;
            aboutNode = restoredResource.adaptTo(Node.class);

        } catch (Exception e) {
            Assert.fail("could not restore deleted");
        }
    }

    @Test
    public void listVersions() {
        try {
            VersionIterator viNullResource = resourceManagement.getVersionIterator(resourceResolver, null);
            Assert.assertNull(viNullResource);
            VersionIterator viNoVersions = resourceManagement.getVersionIterator(resourceResolver, indexRes);
            Assert.assertNull(viNoVersions);
            resourceManagement.createVersion(resourceResolver, indexRes.getPath());
            VersionIterator viOneVersion = resourceManagement.getVersionIterator(resourceResolver, indexRes);
            Assert.assertTrue(viOneVersion.hasNext());
            Version rootversion = (Version) viOneVersion.next();
            Assert.assertNull(rootversion.getLinearPredecessor());
            Assert.assertEquals("jcr:rootVersion", rootversion.getName());
            Version firstversion = (Version) viOneVersion.next();
            Assert.assertNotNull(firstversion.getLinearPredecessor());
            Assert.assertFalse(viOneVersion.hasNext());
            Assert.assertNull(firstversion.getLinearSuccessor());
            Version base = vmPage.getBaseVersion(indexRes.getPath());
            Assert.assertEquals(firstversion.getFrozenNode().getPath(), base.getFrozenNode().getPath());

        } catch (AdminResourceHandler.ManagementException | RepositoryException e) {
            Assert.fail("failed to create or get versions");
        }
    }

    @Test
    public void deleteVersion() {
        try {
            Version version1 = resourceManagement.createVersion(resourceResolver, indexRes.getPath());
            Version version2 = resourceManagement.createVersion(resourceResolver, indexRes.getPath());
            Version baseVersion = vmPage.getBaseVersion(indexRes.getPath());
            Assert.assertEquals(version2.getFrozenNode().getPath(), baseVersion.getFrozenNode().getPath());
            int beforeSize = Iterators.size(
                resourceManagement.getVersionIterator(resourceResolver,indexRes));
            Assert.assertEquals(3, beforeSize);
            VersionIterator viBefore = resourceManagement.getVersionIterator(resourceResolver, indexRes);
            Version rootVersion = viBefore.nextVersion();
            Version versionToBeDeleted = viBefore.nextVersion();
            Assert.assertEquals(version1.getFrozenNode().getPath(), versionToBeDeleted.getFrozenNode().getPath());
            resourceManagement.deleteVersion(resourceResolver, indexRes.getPath(), versionToBeDeleted.getName());
            int afterSize = Iterators.size( resourceManagement.getVersionIterator(resourceResolver,indexRes));
            Assert.assertEquals(2, afterSize);
        } catch (AdminResourceHandler.ManagementException | RepositoryException e) {
            Assert.fail("failed to create or delete version");
        }
    }

    @Test
    public void deletePageForever() {
        final String newPath =  "/content/example/pages/index/about2";
        try {
            resourceManagement.copyResource(
                    resourceResolver,
                    aboutRes,
                    indexRes,
                    "about2",
                    "About 2 be Deleted",
                    null,
                    false
            );
        } catch (AdminResourceHandler.ManagementException e) {
            Assert.fail("failed to setup");
        }
        Resource newRes = resourceResolver.getResource(newPath);
        Assert.assertNotNull(newRes);
        try {
            resourceManagement.deleteResource(resourceResolver, newRes.getPath(), null);
        } catch (AdminResourceHandler.ManagementException e) {
            Assert.fail("failed to delete");
        }
        List<Recyclable> recList = resourceManagement.getRecyclables(resourceResolver, pathPrefix);
        Assert.assertEquals(recList.size(), 1);
        Assert.assertEquals(recList.get(0).getResourcePath(), newPath);
        String frozenNode = recList.get(0).getFrozenNodePath();
        Assert.assertNotNull(resourceResolver.getResource(frozenNode));
        try {
            resourceManagement.deleteResource(resourceResolver, recList.get(0).getResource().getPath(), null);
        } catch (AdminResourceHandler.ManagementException e) {
            logger.error("junit fails", e);
            Assert.fail("failed to delete recyclable ");
        }
    }

    @After
    public void cleanUp() {
        resourceResolver.refresh();
        try {
//            Clean Up Test Nodes
            Node[] nodesToCleanUp = {indexNode, aboutNode, pagesNode};
            for (Node node : nodesToCleanUp) {
                for (NodeType nt : node.getMixinNodeTypes()){
                    if (nt.isNodeType("mix:versionable")) {
                        vmPage.checkout(node.getPath());
                        node.removeMixin("mix:versionable");
                    }
                }
            }
            resourceResolver.commit();

            Node exampleRecycling = JcrUtils.getNodeIfExists(PerConstants.RECYCLE_BIN_PATH + EXAMPLE_SITE_ROOT, jcrSession);
            if (exampleRecycling != null) {
                exampleRecycling.remove();
                jcrSession.save();
            }
        } catch (RepositoryException e) {
            logger.error("test resources were not versionable", e);
        } catch (PersistenceException e) {
            logger.error("Could not restore example", e);
        } finally {
            resourceResolver.close();
            resourceResolver = null;
            jcrSession = null;
        }
    }
}
