package com.peregrine.it.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.peregrine.commons.util.PerConstants.ECMA_DATE_FORMAT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by schaefa on 6/28/17.
 */
public class BasicTestHelpers {

    public static final String ADMIN_PREFIX_URL = "/api/admin/";

    private static final Logger logger = LoggerFactory.getLogger(BasicTestHelpers.class.getName());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();


    public static Map listResourceAsJson(SlingClient client, String path, int level) throws ClientException, IOException {
        SlingHttpResponse response = client.doGet(path + "." + level + ".json", 200);
        assertEquals("Unexpected Mime Type", "application/json;charset=utf-8", response.getFirstHeader("Content-Type").getValue());
        return convertToMap(response);
    }

    public static SlingHttpResponse listResource(SlingClient client, String path,int level) throws ClientException {
        return client.doGet(path + "." + level + ".json", 200);
    }

    public static SlingHttpResponse createFolder(SlingClient client, String path, String name, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createFolder.json" + path;
        logger.info("Create Folder with URL: '{}' and Name: '{}'", url, name);
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static void createFolderStructure(SlingClient client, String path) throws ClientException, IOException {
        String[] folders = path.split("/");
        logger.info("Folders: '{}'", Arrays.asList(folders));
        String folderPath = "";
        for(String folder: folders) {
            if(folder != null && !folder.isEmpty()) {
                String fullFolderPath = folderPath + "/" + folder;
                logger.info("Full Folder Path: '{}'", fullFolderPath);
                SlingHttpResponse response;
                try {
                    Map listResponse = listResourceAsJson(client, fullFolderPath, 0);
                    String primaryType = listResponse.get(JCR_PRIMARY_TYPE) + "";
                    if(
                        "sling:OrderedFolder".equals(primaryType) ||
                        "sling:Folder".equals(primaryType) ||
                        "nt:folder".equals(primaryType)
                    ) {
                        logger.info("Folder is ok: '{}'", listResponse);
                    } else {
                        logger.error("Folder response was not recognized: '{}'", listResponse);
                        fail("Folder response was not recognized: " + listResponse);
                    }
                } catch(ClientException e) {
                    if(e.getHttpStatusCode() == 404) {
                        // Create folder
                        response = createFolder(client, folderPath, folder, 200);
                        logger.info("Create Folder Structure, folder path: '{}', response: '{}'", fullFolderPath, response.getContent());
                        // Check response
                        Map listResponse = convertToMap(response.getContent());
                        checkResponse(listResponse, "type", "folder", "status", "created", "name", folder);
                    } else {
                        throw e;
                    }
                }
                folderPath = fullFolderPath;
            }
        }
    }

    public static Map checkResponse(SlingHttpResponse response, String ... nameThenExpected) throws IOException {
        Map answer = convertToMap(response.getContent());
        checkResponse(answer, nameThenExpected);
        return answer;
    }

    public static void checkResponse(Map response, String ... nameThenExpected) {
        for(int i = 0; i < nameThenExpected.length - 1; i = i + 2) {
            String name = nameThenExpected[i];
            String expected = nameThenExpected[i + 1];
            String actual = response.get(name) + "";
            if(!actual.equals(expected)) {
                logger.info("Property: '{}' yielded: '{}' but did not match expected value: '{}'", name, actual, expected);
                fail("Property: " + name +  " yielded unexpected value: " + actual + " but this was expected: " + expected);
            }
        }
    }

    public static Map convertToMap(SlingHttpResponse response) throws IOException {
        return convertToMap(response == null ? null : response.getContent());
    }

    public static Map convertToMap(String json) throws IOException {
        Map answer = new LinkedHashMap();
        if(json != null) {
            answer = JSON_MAPPER.readValue(json, LinkedHashMap.class);
        }
        return answer;
    }

    public static void checkPages(SlingHttpResponse listResponse, String ... expectedPageNames) throws IOException {
        checkPages(listResponse, false, expectedPageNames);
    }

    public static void checkPages(SlingHttpResponse listResponse, boolean ignoreTitle, String ... expectedPageNames) throws IOException {
        List<String> expectedPageNamesList = new ArrayList<String>();
        if(expectedPageNames != null) {
            for(String name: expectedPageNames) {
                expectedPageNamesList.add(name);
            }
        }
        Map targetFolderMap = convertToMap(listResponse);
        Set<Entry> entries = targetFolderMap.entrySet();
        for(Entry entry: entries) {
            String fieldName = entry.getKey() + "";
            logger.info("Field Name: '{}'", fieldName);
            Object value = entry.getValue();
            logger.info("Item Value: '{}'", value);
            if(value instanceof Map) {
                if(JCR_CONTENT.equals(fieldName)) {
                } else {
                    String expectedPage = expectedPageNamesList.get(0);
                    assertEquals("Unexpected Page (order)", expectedPage, fieldName);
                    Map pageMap = (Map) value;
                    assertEquals("Wrong Page Resource Type", "per:Page", pageMap.get(JCR_PRIMARY_TYPE) + "");
                    Map pageContentMap = (Map) pageMap.get(JCR_CONTENT);
                    assertNotNull("Page Content Node missing", pageContentMap);
                    assertEquals("Unexpected Page Content Type", "per:PageContent", pageContentMap.get(JCR_PRIMARY_TYPE) + "");
                    if(!ignoreTitle) {
                        assertEquals("Unexpected Page Title", expectedPage, pageContentMap.get(JCR_TITLE) + "");
                    }
                    // Found and is ok -> remove
                    expectedPageNamesList.remove(0);
                }
            }
        }
    }

    public static void checkFolders(SlingClient client, String parentPath, String ... expectedFolderNames) throws IOException, ClientException {
        Map targetFolderMap = listResourceAsJson(client, parentPath, 3);
        List<String> expectedFolderNamesList = new ArrayList<String>();
        if(expectedFolderNames != null) {
            for(String name: expectedFolderNames) {
                expectedFolderNamesList.add(name);
            }
        }
        Set<Entry> entries = targetFolderMap.entrySet();
        for(Entry entry: entries) {
            String fieldName = entry.getKey() + "";
            logger.info("Field Name: '{}'", fieldName);
            Object value = entry.getValue();
            logger.info("Item Value: '{}'", value);
            if(value instanceof Map) {
                if(JCR_CONTENT.equals(fieldName)) {
                } else {
                    String expectedFolderName = expectedFolderNamesList.get(0);
                    assertEquals("Unexpected Folder (order)", expectedFolderName, fieldName);
                    Map pageMap = (Map) value;
                    assertEquals("Wrong Folder Resource Type", "sling:OrderedFolder", pageMap.get(JCR_PRIMARY_TYPE) + "");
                    // Found and is ok -> remove
                    expectedFolderNamesList.remove(0);
                }
            }
        }
    }

    public static void createFolders(SlingClient client, String rootFolder, String...folderNames) throws ClientException, IOException {
        for(String folderName: folderNames) {
            createFolder(client, rootFolder, folderName, 200);
            checkFolderExists(client, rootFolder, folderName);
        }
    }

    public static void checkFolderExists(SlingClient client, String rootPath, String folderName) throws IOException, ClientException {
        Map folder = listResourceAsJson(client, rootPath + "/" + folderName, 1);
        String primaryType = folder.get(JCR_PRIMARY_TYPE) + "";
        assertEquals("Wrong Primary Type", "sling:OrderedFolder", primaryType);
    }

    public static Map<String, Map> extractChildNodes(Map source) {
        Map<String, Map> answer = new LinkedHashMap<String, Map>();
        Set<Entry> entries = source.entrySet();
        for(Entry entry: entries) {
            String fieldName = entry.getKey() + "";
            logger.info("Field Name: '{}'", fieldName);
            Object value = entry.getValue();
            logger.info("Item Value: '{}'", value);
            if(value instanceof Map) {
                if(!JCR_CONTENT.equals(fieldName)) {
                    answer.put(fieldName, (Map) value);
                }
            }
        }
        return answer;
    }

//    public static enum CheckType { allProvided, everythingMatches, }
    public static void checkResourceByJson(SlingClient client, String path, int levels, String expectedJson, boolean checkProvided) throws ClientException, IOException {
        SlingHttpResponse response = client.doGet(path + "." + levels + ".json", 200);
        assertEquals("Unexpected Mime Type", "application/json;charset=utf-8", response.getFirstHeader("Content-Type").getValue());
        String jsonResponse = response.getContent();
        ObjectMapper mapper = new ObjectMapper();
        Map expected = convertToMap(expectedJson);
        Map actual = convertToMap(jsonResponse);
        logger.info("Expected Map: '{}'", expected);
        logger.info("Actual Map: '{}'", actual);
        compareJson(expected, actual);
    }

    public static void compareJson(Map<Object, Object> expected, Map actual) throws IOException {
        compareJson(expected, actual, "");
    }
    public static void compareJson(Map<Object, Object> expected, Map actual, String path) throws IOException {
        for(Entry<Object, Object> entry: expected.entrySet()) {
            Object key = entry.getKey() + "";
            assertTrue("Did not find Property with Name: " + key + " (path: " + path + ")", actual.containsKey(key));
            String childPath = path + "/" + key;
            Object value = entry.getValue();
            if(value instanceof Boolean) {
                assertEquals("Boolean Property mismatch. Name: " + key + " (path: " + path + ")", value, actual.get(key));
            } else if(value instanceof Number) {
                assertEquals("Number Property mismatch. Name: " + key + " (path: " + path + ")", value, actual.get(key));
            } else if(value instanceof String) {
                assertEquals("String Property mismatch. Name: " + key + " (path: " + path + ")", value, actual.get(key));
            } else if(value instanceof Object[]) {
                fail("Sling Json response should not contain an array. Name: " + key + " (path: " + path + ")");
            } else if(value instanceof Map) {
                Map expectedChild = (Map) value;
                Map actualChild = (Map) actual.get(key);
                assertNotNull("Child: " + key + " not found as child in response" + " (path: " + path + ")", actualChild);
                compareJson(expectedChild, actualChild, childPath);
            } else {
                fail("Unknown type of value: " + value.getClass() + " (path: " + path + ")");
            }
        }
    }

    public static void checkLastModified(SlingClient client, String path) throws IOException, ClientException, ParseException {
        checkLastModified(client, path, null);
    }

    public static void checkLastModified(SlingClient client, String path, Calendar afterThat) throws IOException, ClientException, ParseException {
        Map actual = listResourceAsJson(client, path, 3);
        // Check if the Last Modified is within a minute and the Last Modified By is set both in the node as well as in the jcr:content if returned
        logger.info("Check Last Modified on Resource: '{}'", path);
        checkLastModifiedOnResource(actual, afterThat);
        if(actual.containsKey(JCR_CONTENT)) {
            Object temp = actual.get(JCR_CONTENT);
            if(temp instanceof Map) {
                logger.info("Check Last Modified on Resource: '{}'", path + "/" + JCR_CONTENT);
                checkLastModifiedOnResource((Map) temp, afterThat);
            } else {
                fail("jcr:content property is not a resource but: " + temp + ", type: " + (temp == null ? "null" : temp.getClass()));
            }
        }
        // Finally check if this is a page or child of and then check the JCR Content Timestamp
        String parentPath = path;
        int index = 0;
        while((index = parentPath.lastIndexOf('/')) > 0) {
            parentPath = parentPath.substring(0, index);
            Map parent = listResourceAsJson(client, parentPath, 1);
            logger.info("Parent Path: '{}', Map: '{}'", parentPath, parent);
            String jcrPrimaryType = parent.get(JCR_PRIMARY_TYPE) + "";
            if(PAGE_CONTENT_TYPE.equals(jcrPrimaryType)) {
                checkLastModifiedOnResource(parent, afterThat);
            } else if(PAGE_PRIMARY_TYPE.equals(jcrPrimaryType)) {
                checkLastModifiedOnResource(parent, afterThat);
                break;
            }
        }
    }

//    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String LONG_ECMA_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    private static final Locale DATE_FORMAT_LOCALE = Locale.US;
    private static DateFormat formatter = new SimpleDateFormat(LONG_ECMA_DATE_FORMAT, DATE_FORMAT_LOCALE);
    private static DateFormat formatter2 = new SimpleDateFormat(ECMA_DATE_FORMAT, DATE_FORMAT_LOCALE);
    private static DateFormat printFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", DATE_FORMAT_LOCALE);

    private static void checkLastModifiedOnResource(Map properties, Calendar afterThat) throws ParseException {
        //AS TODO: convert the response into a Calendar instnce (how?)
        Object lastModifiedValue = properties.get(JCR_LAST_MODIFIED);
        logger.info("Last Modified: '{}'", lastModifiedValue);
        assertNotNull("Last Modified was not set", lastModifiedValue);
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTime(formatter.parse(lastModifiedValue.toString()));
        long differenceInMillis = -1;
        if(afterThat == null) {
            Calendar now = Calendar.getInstance();
            differenceInMillis = getDateDifferenceInMillis(lastModified, false, now, false);
            logger.info("Last Modified Millis Difference: '{}'", differenceInMillis);
            assertTrue("Last Modified is too old", differenceInMillis < 60 * 1000);
        } else {
            // Calendar has millis but the JSon formatted response only seconds. We need to convert them
            // to the same format string and back to a Calendar to compare
            differenceInMillis = getDateDifferenceInMillis(afterThat, true, lastModified, false);
            logger.info("Last Modified Millis Difference: '{}'", differenceInMillis);
            assertTrue("Last Modified is older as given date: " + afterThat.toString(), differenceInMillis > 0);
        }
    }

    /**
     * Get the time differences in dates
     *
     * @param firstDateString Textual Representation of a Date
     * @param second Calendar Date to check against
     * @param truncateSecond If true then the second date is truncated to seconds
     * @return The time differences of the two dates in millis. A positive number means that
     *         the first date is older, 0 means they are equal and negative means that the
     *         first is younger
     * @throws ParseException If the parsing failed
     */
    public static long getDateDifferenceInMillis(String firstDateString, Calendar second, boolean truncateSecond) throws ParseException {
        logger.info("First Date String: '{}'", firstDateString);
        Calendar first = Calendar.getInstance();
        Date time = null;
        boolean truncatFirst = false;
        try {
            time = formatter.parse(firstDateString);
        } catch(ParseException e) {
            // This date format contains milli seconds and so we need to truncate them
            time = formatter2.parse(firstDateString);
            truncatFirst = true;
        }
        first.setTime(time);
        return getDateDifferenceInMillis(first, truncatFirst, second, truncateSecond);
    }

    /**
     * Get the time differences in dates in milli seconds
     * @param first First calendar date entry
     * @param truncateFirst If true the first calendar entry will be truncated to seconds
     * @param second Second calendar date entry
     * @param truncateSecond If true the second calendar entry will be truncated to seconds
     * @return The time differences of the two dates in millis. A positive number means that
     *         the first date is older, 0 means they are equal and negative means that the
     *         first is younger
     * @throws ParseException If the parsing failed
     */
    public static long getDateDifferenceInMillis(Calendar first, boolean truncateFirst, Calendar second, boolean truncateSecond) throws ParseException {
        if(truncateFirst) {
            String firstDateString = formatter.format(first.getTime());
            logger.info("First Date Format: '{}'", firstDateString);
            first = Calendar.getInstance();
            first.setTime(formatter.parse(firstDateString));
        } else {
            logger.info("Untruncated First Date Format: '{}'", printFormatter.format(first.getTime()));
        }
        if(truncateSecond) {
            String secondDateString = formatter.format(second.getTime());
            logger.info("Second Date Format: '{}'", secondDateString);
            second = Calendar.getInstance();
            second.setTime(formatter.parse(secondDateString));
        } else {
            logger.info("Untruncated Second Date Format: '{}'", printFormatter.format(first.getTime()));
        }
        long answer = Duration.between(first.toInstant(), second.toInstant()).toMillis();
        logger.info("Date Difference in millis: '{}'", answer);
        return answer;
    }

    public static Calendar createTimestampAndWait() {
        Calendar answer = Calendar.getInstance();
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            // Ignore
        }
        return answer;
    }
}
