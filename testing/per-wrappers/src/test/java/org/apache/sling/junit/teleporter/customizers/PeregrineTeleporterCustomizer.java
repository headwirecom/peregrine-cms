package org.apache.sling.junit.teleporter.customizers;

import com.peregrine.it.basic.TestConstants;
import org.apache.sling.junit.rules.TeleporterRule;
import org.apache.sling.testing.clients.util.TimeoutsProvider;
import org.apache.sling.testing.serversetup.instance.SlingTestBase;
import org.apache.sling.testing.teleporter.client.ClientSideTeleporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by schaefa on 7/5/17.
 */
public class PeregrineTeleporterCustomizer
    implements TeleporterRule.Customizer
{
    private static final Logger logger = LoggerFactory.getLogger(PeregrineTeleporterCustomizer.class.getName());

    private final static SlingTestBase S = new SlingTestBase();

    @Override
    public void customize(TeleporterRule t, String options) {
        logger.info("Start the Peregrine Teleporter Customizer");
        final ClientSideTeleporter cst = (ClientSideTeleporter)t;
        cst.setBaseUrl(S.getServerBaseUrl());
        cst.setServerCredentials(S.getServerUsername(), S.getServerPassword());
        cst.setTestReadyTimeoutSeconds(TimeoutsProvider.getInstance().getTimeout(10));
        // Add the Test Constants class to the bundle in order to make it accessible
        cst.embedClass(TestConstants.class);
        logger.info("End the Peregrine Teleporter Customizer");
    }
}
