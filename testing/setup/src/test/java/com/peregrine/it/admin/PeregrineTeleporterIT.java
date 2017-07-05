package com.peregrine.it.admin;

//import org.apache.sling.api.resource.LoginException;
//import org.apache.sling.api.resource.Resource;
//import org.apache.sling.api.resource.ResourceResolver;
//import org.apache.sling.api.resource.ResourceResolverFactory;
//import org.apache.sling.junit.rules.TeleporterRule;
//import org.junit.Rule;
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static org.junit.Assert.assertNotNull;

/**
 * Created by schaefa on 7/5/17.
 */
public class PeregrineTeleporterIT
//    extends AbstractTest
{

////    private static final Logger logger = LoggerFactory.getLogger(PeregrineTeleporterIT.class.getName());
//
//    public static final String ROOT_PATH = "/content/tests";
//
//    @Rule
//    public final TeleporterRule teleporter = TeleporterRule.forClass(getClass(), "PeregrineTeleporter");
//
//    @Test
//    public void testTestFolders() throws IOException, LoginException {
////        logger.info("Start Test Folders Test");
//        // demonstrate that we can access OSGi services from such
//        // teleported tests
//        final ResourceResolverFactory resourceResolverFactory = teleporter.getService(ResourceResolverFactory.class);
////        logger.info("Got Resource Resolver Factory: '{}'", resourceResolverFactory);
//        assertNotNull("Teleporter should provide a Resource Resolver Factory", resourceResolverFactory);
//        ResourceResolver resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
////        logger.info("Got Resource Resolver: '{}'", resourceResolver);
//        assertNotNull("Wasn't able to create an Admin Resource Resolver", resourceResolver);
////        Resource testFolder = resourceResolver.getResource(ROOT_PATH);
////        logger.info("Got Test Folder: '{}'", testFolder);
////        assertNotNull("Test Folder is not found", testFolder);
////        Iterable<Resource> testFolders = testFolder.getChildren();
////        for(Resource child: testFolders) {
////            logger.info("Test Folder child: '{}'", child.getPath());
////        }
//    }
////
////    @Override
////    public Logger getLogger() {
////        return logger;
////    }
}
