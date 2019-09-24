package com.peregrine.commons.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void compareJsonTexts(String expectedJson, String actualJson) throws IOException {
        compareJsonTexts(expectedJson, actualJson, false);
    }

    public static void compareJsonTexts(String expectedJson, String actualJson, boolean ignoreNames) throws IOException {
        Map expected = convertJsonTextToMap(expectedJson);
        Map actual = convertJsonTextToMap(actualJson);
        compareJson(expected, actual, ignoreNames);
    }

    public static Map convertJsonTextToMap(String json) throws IOException {
        Map answer = new LinkedHashMap();
        if(json != null) {
            answer = OBJECT_MAPPER.readValue(json, LinkedHashMap.class);
        }
        return answer;
    }

    public static List convertJsonTextToList(String json) throws IOException {
        List answer = new ArrayList();
        if(json != null) {
            answer = OBJECT_MAPPER.readValue(json, ArrayList.class);
        }
        return answer;
    }

    public static void compareJson(Map<Object, Object> expected, Map actual) throws IOException {
        compareJson(expected, actual, "", false);
    }
    public static void compareJson(Map<Object, Object> expected, Map actual, boolean ignoreName) throws IOException {
        compareJson(expected, actual, "", ignoreName);
    }
    public static void compareJson(Map<Object, Object> expected, Map actual, String path, boolean ignoreName) throws IOException {
        for(Entry<Object, Object> entry: expected.entrySet()) {
            Object key = entry.getKey() + "";
            assertTrue("Did not find Map Entry with Name: " + key + " (path: " + path + ")", actual.containsKey(key));
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
                compareJson(expectedChild, actualChild, childPath, ignoreName);
            } else if(value instanceof List) {
                List expectedlist = (List) value;
                List actualList = (List) actual.get(key);
                List actualList2 = new ArrayList(actualList);
                for(Object temp : expectedlist) {
                    if(temp instanceof Map) {
                        Map expectedListMap = (Map) temp;
                        Map actualListMap = null;
                        if(!ignoreName) {
                            String name = (String) expectedListMap.get("name");
                            if(name == null) {
                                fail("Expected List Map entry has no name: " + expectedListMap);
                            } else {
                                for (Object temp2 : actualList2) {
                                    if (temp2 instanceof Map) {
                                        Map tempMap = (Map) temp2;
                                        String name2 = (String) tempMap.get("name");
                                        if (name2 == null) {
                                            fail("Given List Map entry has no name: " + tempMap);
                                        }
                                        if (name.equals(name2)) {
                                            actualListMap = tempMap;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(actualListMap == null) {
                                fail("No Actual List Map Entry found for: " + name);
                            }
                        } else {
                            // When we ignore the name then we try to find a map entry in the actual
                            // map that contains all entries from the expected map
                            for(Object temp2 : actualList2) {
                                if(temp2 instanceof Map) {
                                    Map tempMap = (Map) temp2;
                                    boolean found = true;
                                    for(Object expectedKey : expectedListMap.keySet()) {
                                        if(tempMap.containsKey(expectedKey)) {
                                            Object expectedValue = expectedListMap.get(expectedKey);
                                            Object actualValue = tempMap.get(expectedKey);
                                            if(!(expectedValue == null && actualValue == null) && !(expectedValue.equals(actualValue))) {
                                                found = false;
                                                break;
                                            }
                                        }
                                    }
                                    if(found) {
                                        actualListMap = tempMap;
                                        break;
                                    }
                                }
                            }
                            if(actualListMap == null) {
                                fail("No Actual List Map Entry found for Expected Map: " + expectedListMap);
                            }
                        }
                        actualList2.remove(actualListMap);
                        compareJson(expectedListMap, actualListMap, path, ignoreName);
                    } else if(temp instanceof String) {
                        String item = (String) temp;
                        String actualItem = null;
                        for(Object temp2 : actualList2) {
                            if(temp2 instanceof String) {
                                String temp2a = (String) temp2;
                                if(item.equals(temp2a)) {
                                    actualItem = temp2a;
                                    break;
                                }
                            }
                        }
                        if(actualItem != null) {
                            actualList2.remove(actualItem);
                        } else {
                            fail("No actual list item found for: " + item);
                        }
                    } else {
                        fail("Unknown type of list value: " + temp.getClass() + " (path: " + path + ")");
                    }
                }
                if(!actualList2.isEmpty()) {
                    fail("Actual List has more entries: " + actualList2);
                }
            } else if(value == null) {
                Object expectedNull = actual.get(key);
                assertNull("Expected a null value", expectedNull);
            } else {
                fail("Unknown type of value: '" + (value == null ? "null" : value.getClass()) + "' (path: " + childPath + ")");
            }
        }
    }
}
