/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.peregrine.slingjunit;

import org.apache.felix.utils.json.JSONParser;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.junit.impl.servlet.SlingJUnitServlet;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.apache.sling.testing.paxexam.TestSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SlingJunitIT extends TestSupport {

    private static final int STARTER_HTTP_PORT = Integer.getInteger("starter.http.port", 8080);
    private static final int STARTER_MIN_BUNDLES_COUNT = Integer.getInteger("starter.min.bundles.count", Integer.MAX_VALUE);
    protected static OsgiConsoleClient CLIENT;
    private final static int STARTUP_WAIT_SECONDS = 30;
    private final static String VERSIONS_TEST = "/system/sling/junit/com.peregrine.slingjunit.VersionsJTest.html";
    private final static String ADAPTION_TEST = "/system/sling/junit/com.peregrine.slingjunit.AdaptionJTest.html";
    private final static String REFERENCES_TEST = "/system/sling/junit/com.peregrine.slingjunit.ReferencesJTest.html";



//    @ClassRule
//    public static StarterReadyRule LAUNCHPAD = new StarterReadyRule(STARTER_HTTP_PORT);
    private HttpClientContext httpClientContext;

    @Before
    public void prepareHttpContext() {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("admin", "admin");
        credsProvider.setCredentials(new AuthScope("localhost", STARTER_HTTP_PORT), creds);

        BasicAuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost("localhost", STARTER_HTTP_PORT, "http"), basicAuth);

        httpClientContext = HttpClientContext.create();
        httpClientContext.setCredentialsProvider(credsProvider);
        httpClientContext.setAuthCache(authCache);
    }

    private CloseableHttpClient newClient() {

        return HttpClientBuilder.create()
                .setDefaultCredentialsProvider(httpClientContext.getCredentialsProvider())
                .build();
    }

    @Test
    public void verifyAllBundlesStarted() throws Exception {

        try ( CloseableHttpClient client = newClient() ) {

            HttpGet get = new HttpGet("http://localhost:" + STARTER_HTTP_PORT + "/system/console/bundles.json");

            // pass the context to ensure preemptive basic auth is used
            // https://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
            try ( CloseableHttpResponse response = client.execute(get, httpClientContext) ) {

                if ( response.getStatusLine().getStatusCode() != 200 ) {
                    fail("Unexpected status line " + response.getStatusLine());
                }

                Header contentType = response.getFirstHeader("Content-Type");
                assertThat("Content-Type header", contentType.getValue(), CoreMatchers.startsWith("application/json"));

                Map<String, Object> obj = new JSONParser(response.getEntity().getContent()).getParsed();

                @SuppressWarnings("unchecked")
                List<Object> status = (List<Object>) obj.get("s");

                @SuppressWarnings("unchecked")
                List<Object> bundles = (List<Object>) obj.get("data");
                if(bundles.size() < STARTER_MIN_BUNDLES_COUNT) {
                    fail("Expected at least " + STARTER_MIN_BUNDLES_COUNT + " bundles, got " + bundles.size());
                }

                BundleStatus bs = new BundleStatus(status);

                if ( bs.resolvedBundles != 0 || bs.installedBundles != 0 ) {

                    StringBuilder out = new StringBuilder();
                    out.append("Expected all bundles to be active, but instead got ")
                        .append(bs.resolvedBundles).append(" resolved bundles, ")
                        .append(bs.installedBundles).append(" installed bundlles: ");

                    for ( int i = 0 ; i < bundles.size(); i++ ) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> bundle = (Map<String, Object>) bundles.get(i);

                        String bundleState = (String) bundle.get("state");
                        String bundleSymbolicName = (String) bundle.get("symbolicName");
                        String bundleVersion = (String) bundle.get("version");

                        switch ( bundleState ) {
                            case "Active":
                            case "Fragment":
                                continue;

                            default:
                                out.append("\n- ").append(bundleSymbolicName).append(" ").append(bundleVersion).append(" is in state " ).append(bundleState);
                        }
                    }

                    fail(out.toString());
                }
            }
        }
    }

    @Test
    public void ensureRepositoryIsStarted() throws Exception {

        try ( CloseableHttpClient client = newClient() ) {

            HttpGet get = new HttpGet("http://localhost:" + STARTER_HTTP_PORT + "/server/default/jcr:root/content");

            try ( CloseableHttpResponse response = client.execute(get) ) {

                if ( response.getStatusLine().getStatusCode() != 200 ) {
                    fail("Unexpected status line " + response.getStatusLine());
                }

                Header contentType = response.getFirstHeader("Content-Type");
                assertThat("Content-Type header", contentType.getValue(), equalTo("text/xml"));

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(response.getEntity().getContent());

                Element docElement = document.getDocumentElement();
                NamedNodeMap attrs = docElement.getAttributes();

                Node nameAttr = attrs.getNamedItemNS("http://www.jcp.org/jcr/sv/1.0", "name");
                assertThat("no 'name' attribute found", nameAttr, notNullValue());
                assertThat("Invalid name attribute value", nameAttr.getNodeValue(), equalTo("content"));
            }
        }
    }

    @Test
    public void runSlingJunitVersionsSpec() throws Exception {
        SlingHttpResponse response = CLIENT.doPost(VERSIONS_TEST,
                new StringEntity("some text"),
                Collections.emptyList(),
                200);
        response.checkContentContains("TEST RUN FINISHED");
        response.checkContentContains("failures:0");
        response.checkContentContains("ignored:0");
        response.checkContentContains("tests:14");
    }

    @Test
    public void runSlingJunitAdaptionSpec() throws Exception {
        SlingHttpResponse response = CLIENT.doPost(ADAPTION_TEST,
                new StringEntity("some text"),
                Collections.emptyList(),
                200);
        response.checkContentContains("TEST RUN FINISHED");
        response.checkContentContains("failures:0");
        response.checkContentContains("ignored:0");
        response.checkContentContains("tests:8");
    }

    @Test
    public void runSlingJunitReferencesSpec() throws Exception {
        SlingHttpResponse response = CLIENT.doPost(REFERENCES_TEST,
                new StringEntity("some text"),
                Collections.emptyList(),
                200);
        response.checkContentContains("TEST RUN FINISHED");
        response.checkContentContains("failures:0");
        response.checkContentContains("ignored:0");
        response.checkContentContains("tests:2");
    }

    @BeforeClass
    public static void waitForSling() throws Exception {
        final URI url = new URI(String.format("http://localhost:%d", STARTER_HTTP_PORT));
        CLIENT = new OsgiConsoleClient(url, "admin", "admin");
        CLIENT.waitExists("/", STARTUP_WAIT_SECONDS * 1000, 500);
        CLIENT.waitComponentRegistered(SlingJUnitServlet.class.getName(), 10 * 1000, 500);
        // Verify stable status for a bit
        for(int i=0; i < 10 ; i++) {
            CLIENT.waitComponentRegistered(SlingJUnitServlet.class.getName(), 1000, 100);
            Thread.sleep(100);
        }
    }

    static class BundleStatus {

        long totalBundles;
        long activeBundles;
        long activeFragments;
        long resolvedBundles;
        long installedBundles;

        public BundleStatus(List<Object> array) {

            totalBundles = (Long)array.get(0);
            activeBundles = (Long)array.get(1);
            activeFragments = (Long)array.get(2);
            resolvedBundles = (Long)array.get(3);
            installedBundles = (Long)array.get(4);

        }
    }
}
