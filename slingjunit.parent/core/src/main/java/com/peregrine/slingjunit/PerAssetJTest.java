package com.peregrine.slingjunit;

import com.peregrine.adaption.PerAsset;
import com.peregrine.admin.resource.AdminResourceHandler;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.awt.*;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SlingAnnotationsTestRunner.class)
public class PerAssetJTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @TestReference
    AdminResourceHandler resourceManagement;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver resourceResolver;
    private Resource resource;
    private PerAsset perAsset;
    private static String ASSET_PNG = "/content/example/assets/images/logo.png";
    private static String ASSET_JPG = "/content/example/assets/images/anchored.jpg";
    private static String ASSET_SVG = "/content/example/assets/images/instagram.svg";
    private static String PER_DATA = "jcr:content/metadata/per-data";
    @Test
    public void dimensionsPng() {
        resource = resourceResolver.getResource(ASSET_PNG);
        perAsset = resource.adaptTo(PerAsset.class);
        assertDimensions();
    }

    @Test
    public void dimensionsJpg(){
        resource = resourceResolver.getResource(ASSET_JPG);
        perAsset = resource.adaptTo(PerAsset.class);
        assertDimensions();
    }

    @Test
    public void dimensionsSvg(){
        resource = resourceResolver.getResource(ASSET_SVG);
        perAsset = resource.adaptTo(PerAsset.class);
        assertDimensions();
    }

    private void assertDimensions(){
        try {
            perAsset.setDimension();
            resourceResolver.refresh();
            resourceResolver.commit();
            Dimension dimension = perAsset.getDimension();
            assertTrue(dimension.getHeight() > 0);
            assertTrue(dimension.getWidth() > 0);
        } catch (RepositoryException e) {
            fail("could not save dimensions");
        } catch (IOException e) {
            fail("could not save dimensions");
        }
    }

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
    }

    @After
    public void cleanup() {
        Resource perData = resource.getChild(PER_DATA);
        try {
            resourceResolver.delete(perData);
        } catch (PersistenceException e) {
            logger.error("could reset dimensions", e);
            fail("could reset dimensions");
        }
        resourceResolver.close();
        resourceManagement = null;
        resolverFactory = null;
        resource = null;
        perAsset = null;
    }
}
