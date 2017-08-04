/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.peregrine.nodejs.servlet;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.testing.tools.http.Request;
import org.apache.sling.testing.tools.http.RequestExecutor;
import org.apache.sling.testing.tools.http.RetryingContentChecker;
import org.apache.sling.testing.tools.retry.RetryLoop;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.apache.sling.testing.tools.sling.TimeoutsProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.fail;

// NOTE: This Annotation requires jUnit 4.11 or newer
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NPMPackageManagerServletIT
    extends SlingTestBase
{
    public static final String SERVLET_BASE_PATH =  "/api/nodejs";
    public static final String SERVLET_LIST_PATH =  SERVLET_BASE_PATH + "/list";
    public static final String SERVLET_INSTALL_PATH =  SERVLET_BASE_PATH + "/package/install";
    public static final String SERVLET_REMOVE_PATH =  SERVLET_BASE_PATH + "/package/remove";

    public static final String NPM_PACKAGE = "npm";
    public static final String CAMEL_CASE_PACKAGE = "camelcase";
    public static final String FS_PACKAGE = "fs";

    protected final static Logger LOGGER = LoggerFactory.getLogger(NPMPackageManagerServletIT.class);

//    @ClassRule
//    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @Rule
    public TestName name = new TestName();
    @Rule
    public ExceptionLoggingRule exceptionLoggingRule = new ExceptionLoggingRule(LOGGER);
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() throws Exception {
        LOGGER.info("\n\n---------------------- Start Test Class --------------\n\n");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LOGGER.info("\n\n---------------------- End Test Class --------------\n\n");
    }

    @Before
    public void setUp() throws Exception {
        LOGGER.info("\n\nStart Test Case: '{}' --------------\n\n", name.getMethodName());
        // Make sure NPM is installed
        TestResponse response = callServlet(SERVLET_LIST_PATH, false, "{", "\"dependencies\"", "}");
        String content = response.getResponse();
        try {
            JSONObject json = new JSONObject(content);
            JSONObject dependencies = json.getJSONObject("dependencies");
            LOGGER.info("Dependencies object: '{}'", dependencies);
            Iterator<String> i = dependencies.keys();
            boolean found = false;
            while(i.hasNext()) {
                String packageName = i.next();
                if(NPM_PACKAGE.equals(packageName)) {
                    // Required so ignore
                    found = true;
                }
            }
            if(!found) {
                fail(NPM_PACKAGE + " Package not found");
            }
        } catch(JSONException e) {
            LOGGER.error("Returned response is not a JSon string", e);
            fail("Returned response is not a JSon string: " + e);
        }
    }

    @After
    public void tearDown() throws Exception {
        LOGGER.info("\n\nEnd Test Case: '{}' --------------\n\n", name.getMethodName());
    }

    /*
     * ATTENTION: The tests must be executed in order as there are state dependencies.
     *            For that all tests must be order alphabetical in ascending order which
     *            is done with the 'testX_' prefix where X is replaced with one or more
     *            character sorting the tests in Order.
     */
    @Test
    public void testA_InstallSinglePackage() {
        removeExistingPackages();

        TestResponse response = modifyPackage(true, "camelcase");
        String content = response.getResponse();
        try {
            JSONObject json = new JSONObject(content);
        } catch(JSONException e) {
            LOGGER.error("Returned response is not a JSon string", e);
            fail("Returned response is not a JSon string: " + e);
        }
    }

    @Test
    public void testB_ListSinglePackage() {
        TestResponse response = callServlet(SERVLET_LIST_PATH, false, "{", "\"dependencies\"", "}");
        if(response.getTimedOut()) {
            fail("Request timed out");
        }
        // No need to handle content but we LOGGER it here
        LOGGER.info("Request Content: '{}'", response.getResponse());
        String content = response.getResponse();
        try {
            JSONObject json = new JSONObject(content);
            JSONObject dependencies = json.getJSONObject("dependencies");
            LOGGER.info("Dependencies object: '{}'", dependencies);
            Iterator<String> i = dependencies.keys();
            boolean found = false;
            while(i.hasNext()) {
                String packageName = i.next();
                if(NPM_PACKAGE.equals(packageName)) {
                    // Required so ignore
                } else
                if(CAMEL_CASE_PACKAGE.equals(packageName)) {
                    found = true;
                } else
                {
                    fail("Found another package: " + packageName);
                }
            }
            if(!found) {
                fail(CAMEL_CASE_PACKAGE + " Package not found");
            }
        } catch(JSONException e) {
            LOGGER.error("Returned response is not a JSon string", e);
            fail("Returned response is not a JSon string: " + e);
        }
    }

    @Test
    public void testC_InstallSecondPackage() {
        TestResponse response = modifyPackage(true, "fs");
        String content = response.getResponse();
        try {
            JSONObject json = new JSONObject(content);
        } catch(JSONException e) {
            LOGGER.error("Returned response is not a JSon string", e);
            fail("Returned response is not a JSon string: " + e);
        }
    }

    @Test
    public void testD_ListTwoPackages() {
        TestResponse response = callServlet(SERVLET_LIST_PATH, false, "{", "\"dependencies\"", "}");
        if(response.getTimedOut()) {
            fail("Request timed out");
        }
        // No need to handle content but we LOGGER it here
        LOGGER.info("Request Content: '{}'", response.getResponse());
        String content = response.getResponse();
        try {
            JSONObject json = new JSONObject(content);
            JSONObject dependencies = json.getJSONObject("dependencies");
            LOGGER.info("Dependencies object: '{}'", dependencies);
            Iterator<String> i = dependencies.keys();
            List<String> expected = new ArrayList<String>(Arrays.asList(NPM_PACKAGE, CAMEL_CASE_PACKAGE, FS_PACKAGE));
            while(i.hasNext()) {
                String packgeName = i.next();
                if(!expected.contains(packgeName)) {
                    fail("Found an unexpected Package: " + packgeName);
                } else {
                    expected.remove(packgeName);
                }
            }
            if(!expected.isEmpty()) {
                fail("Did not find these expected packages: " + expected);
            }
        } catch(JSONException e) {
            LOGGER.error("Returned response is not a JSon string", e);
            fail("Returned response is not a JSon string: " + e);
        }
    }

    /**
     * This test is both testing List and Remove
     */
//    @Test
    public void removeExistingPackages() {
        TestResponse response = callServlet(SERVLET_LIST_PATH, false, "{", "}");
        if(response.getTimedOut()) {
            fail("Request timed out");
        }
        // No need to handle content but we LOGGER it here
        LOGGER.info("Request Content: '{}'", response.getResponse());
        String content = response.getResponse();
        try {
            JSONObject json = new JSONObject(content);
            if(json.has("dependencies")) {
                JSONObject dependencies = json.getJSONObject("dependencies");
                LOGGER.info("Dependencies object: '{}'", dependencies);
                Iterator<String> i = dependencies.keys();
                while(i.hasNext()) {
                    String key = i.next();
                    if(!key.equals(NPM_PACKAGE)) {
                        modifyPackage(false, key);
                    }
                }
            }
        } catch(JSONException e) {
            LOGGER.error("Returned response is not a JSon string", e);
            fail("Returned response is not a JSon string: " + e);
        }
    }

    private TestResponse modifyPackage(boolean doInstall, String packageName) {
        LOGGER.info((doInstall ? "Install" : "Remove") + " Package: '{}'", packageName);
        TestResponse answer = callServlet(
            (
                doInstall ?
                    SERVLET_INSTALL_PATH :
                    SERVLET_REMOVE_PATH
            )
                + "?name=" + packageName,
            true, "{", "\"message\"", "\"output\"", "}"
        );
        LOGGER.info("Modification Response: '{}'", answer.getResponse());
        return answer;
    }

    private TestResponse callServlet(final String path, final boolean doPost, final String ... testSnippets) {
        final TestResponse answer = new TestResponse();
        final RetryingContentChecker servletChecker = new PostRetryingContentChecker(path, doPost, answer, testSnippets);

        LOGGER.info("Got Retrying Content Checker: '{}'", servletChecker);
        LOGGER.info("Timed Out: '{}'", answer.getTimedOut());

        final int status = 200;
        final int timeout = TimeoutsProvider.getInstance().getTimeout(1000);
        LOGGER.info("Got Timeout: '{}'", timeout);
        final int intervalMsec = TimeoutsProvider.getInstance().getTimeout(500);
        LOGGER.info("Got Interval: '{}'", intervalMsec);
        LOGGER.info("Checking that {} returns status {}, timeout={} seconds",
            new Object[] { path, status, timeout });
        servletChecker.check(path, status, timeout, intervalMsec);

        return answer;
    }

    private class PostRetryingContentChecker
        extends RetryingContentChecker
    {
        private boolean doPost;
        private TestResponse response;
        private String[] testSnippets;
        private int retries = 10;

        public PostRetryingContentChecker(String path, boolean doPost, TestResponse response, String ... testSnippets) {
            super(getRequestExecutor(), getRequestBuilder(), getServerUsername(), getServerPassword());
            this.doPost = doPost;
            this.response = response;
            this.testSnippets = testSnippets;
            LOGGER.info("PRCC created, post: '{}', response: '{}', test snippet: '{}'", doPost, response, testSnippets);
        }

        public void check(final String path, final int expectedStatus, int timeoutSeconds, int intervalBetweenrequestsMsec) {
            LOGGER.info("PRCC Path: '{}'", path);
            final RetryLoop.Condition c = new RetryLoop.Condition() {
                public String getDescription() {
                    return "Expecting " + path + " to return HTTP status " + expectedStatus;
                }

                public boolean isTrue() throws Exception {
                    Request request;
                    if(doPost) {
                        LOGGER.info("Create Post RCC");
                        request = getRequestBuilder().buildPostRequest(path);
                    } else {
                        LOGGER.info("Create Get RCC, retries: '{}'", retries);
                        request = getRequestBuilder().buildGetRequest(path);
                    }
                    RequestExecutor answer = getRequestExecutor().execute(request.withCredentials(getServerUsername(), getServerPassword()));
                    return assertMore(getRequestExecutor());
                }

            };
            new RetryLoop(c, timeoutSeconds, intervalBetweenrequestsMsec) {
                @Override
                protected void onTimeout() {
                    PostRetryingContentChecker.this.onTimeout();
                }
            };
        }
        @Override
        public void onTimeout() {
            response.setTimedOut(true);
        }

        @Override
        protected boolean assertMore(RequestExecutor e) throws Exception {
            try {
                LOGGER.info("Assert More, Content: '{}'", e.getContent());
                e.assertContentContains(testSnippets);
                response.setResponse(e.getContent());
                LOGGER.info("Exit due to expected response");
                // Succeeded -> exit
                return true;
            } catch(AssertionError ae) {
                // Wrong content -> throw it until we are out of repetitions
                if(retries > 0) {
                    throw ae;
                } else {
                    response.setResponse(e.getContent());
                    LOGGER.info("Exit due to max of retries");
                    return true;
                }
            } finally {
                retries--;
                LOGGER.info("Remaining Retries: '{}'", retries);
            }
        }

    }

    private static class TestResponse {
        private boolean timedOut = false;
        private String response = "";

        public String getResponse() {
            return response;
        }

        public TestResponse setResponse(String response) {
            this.response = response;
            return this;
        }

        public boolean getTimedOut() {
            return timedOut;
        }

        public TestResponse setTimedOut(boolean value) {
            this.timedOut = value;
            return this;
        }
    }
}