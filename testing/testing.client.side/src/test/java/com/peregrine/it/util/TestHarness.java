package com.peregrine.it.util;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.peregrine.commons.util.PerConstants.FROM_SITE_NAME;
import static com.peregrine.commons.util.PerConstants.TO_SITE_NAME;
import static com.peregrine.it.basic.BasicTestHelpers.checkFolder;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by Andreas Schaefer on 6/28/17.
 */
public class TestHarness {

    public static final String ADMIN_PREFIX_URL = "/perapi/admin/";

    private static final Logger logger = LoggerFactory.getLogger(TestHarness.class.getName());

    public static boolean deleteLeafFolder(SlingClient client, String folderPath) {
        if(isEmpty(folderPath)) {
            logger.warn("Given Folder Path is empty");
            return false;
        }
        if(!folderPath.startsWith("/")) {
            logger.warn("Given Folder Path: '{}' is not absolute", folderPath);
            return false;
        }
        try {
            String[] folders = folderPath.split("/");
            String parentPath = "";
            boolean found = true;
            for(String folder: folders) {
                if(isNotEmpty(folder)) {
                    found = checkFolder(client, parentPath, folder);
                    if(!found) {
                        break;
                    }
                    parentPath += "/" + folder;
                }
            }
            if(found) {
                deleteFolder(client, folderPath, 200);
            }
        } catch(ClientException e) {
            logger.warn("Could not delete leaf folder, path: '{}' -> ignore", folderPath, e);
            return false;
        } catch(IOException e) {
            logger.warn("Could not delete leaf folder, path: '{}' -> ignore", folderPath, e);
            return false;
        }
        return true;
    }

//    public static SlingHttpResponse nodeExists(SlingClient client, String path) throws ClientException, IOException {
//        SlingHttpResponse answer = null;
//        String parent = path;
//        String name = "";
//        if(!"/".equals(path)) {
//            if(path.endsWith("/")) {
//                path = path.substring(0, path.length() - 1);
//            }
//            int index = path.lastIndexOf('/');
//            if(index < 0) {
//                logger.error("Given path: '{}' is not absolute -> ignored and return null", path);
//                return answer;
//            } else if(index > 0){
//                parent = path.substring(0, index);
//                name = path.substring(index + 1);
//            }
//        }
//        if(isEmpty(name)) { return answer; }
//        String url = parent + ".1.json";
//        logger.info("List Nodes of Parent: '{}'", url);
//        try {
//            return client.doGet(url, 200);
//        } catch(ClientException e) {
//            logger.error("Could not list parent -> ignore and return null");
//            return answer;
//        }
//    }

    public static SlingHttpResponse deleteFolder(SlingClient client, String path, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "deleteNode.json" + path;
        logger.info("Delete Folder with URL: '{}' and Name: '{}'", url);
        HttpEntity formEntry = FormEntityBuilder.create().build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse deleteNode(SlingClient client, String path, int expectedStatus) throws ClientException, IOException {
        return deleteNode(client, path, null, expectedStatus);
    }

    public static SlingHttpResponse deleteNode(SlingClient client, String path, String type, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "deleteNode.json" + path;
        logger.info("Delete Node with URL: '{}' and Name: '{}'", url);
        HttpEntity formEntry = null;
        if(type == null) {
            formEntry = FormEntityBuilder.create().build();
        } else {
            formEntry = FormEntityBuilder.create().addParameter("type", type).build();
        }
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse deletePage(SlingClient client, String path, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "deletePage.json" + path;
        logger.info("Delete Page with URL: '{}' and Name: '{}'", url);
        HttpEntity formEntry = FormEntityBuilder.create().build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse createPage(SlingClient client, String path, String name, String templatePath, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createPage.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).addParameter("templatePath", templatePath).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse createObject(SlingClient client, String path, String name, String templatePath, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createObject.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).addParameter("templatePath", templatePath).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse createTemplate(SlingClient client, String path, String name, String component, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createTemplate.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).addParameter("component", component).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse createSite(SlingClient client, String fromName, String toName, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createSite.json";
        HttpEntity formEntry = FormEntityBuilder.create()
            .addParameter(FROM_SITE_NAME, fromName)
            .addParameter(TO_SITE_NAME, toName)
            .build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse moveResource(SlingClient client, String from, String to, String type, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "move.json" + from;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("to", to).addParameter("type", type).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse renameResource(SlingClient client, String from, String to, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "rename.json" + from;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("to", to).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse moveNodeToResource(SlingClient client, String from, String to, String type, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "moveNodeTo.json" + to;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("component", from).addParameter("type", type).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse insertNodeAtAsComponent(SlingClient client, String path, String component, String type, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "insertNodeAt.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("component", component).addParameter("type", type).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse insertNodeAtAsContent(SlingClient client, String path, String content, String type, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "insertNodeAt.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("content", content).addParameter("type", type).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse insertNodeAtAsContent(SlingClient client, String path, String content, String type, String variation, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "insertNodeAt.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("content", content).addParameter("type", type).addParameter("variation", variation).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse getReferenceList(SlingClient client, String path, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "ref.json" + path;
        return client.doGet(url, expectedStatus);
    }

    public static SlingHttpResponse executeReplication(SlingClient client, String path, String name, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "repl.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse executeDeepReplication(SlingClient client, String path, String name, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "repl.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).addParameter("deep", "true").build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse uploadFile(SlingClient client, String path, String name, byte[] content, int expectedStatus) throws ClientException, IOException {
        return uploadFile(client, path, name, content, "application/octet-stream", expectedStatus);
    }

    public static SlingHttpResponse uploadFile(SlingClient client, String path, String name, byte[] content, String contentType, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "uploadFiles.json" + path;
        HttpEntity entity = MultipartEntityBuilder.create()
            .addBinaryBody(name, content, ContentType.create(contentType), name)
            .build();
        return client.doPost(url, entity, expectedStatus);
    }

    public static SlingHttpResponse renderAsset(SlingClient client, String path, boolean doPost, String renditionName, int expectedStatus) throws ClientException, IOException {
        String url = path;
        if(isNotEmpty(renditionName)) {
            url = path + ".rendition.json/" + renditionName;
        }
        HttpEntity formEntry = FormEntityBuilder.create().build();
        // return the sling response
        if(doPost) {
            return client.doPost(url, formEntry, expectedStatus);
        } else {
            return client.doGet(url, expectedStatus);
        }
    }

    public static SlingHttpResponse updateResource(SlingClient client, String path, String content, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "updateResource.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("content", content).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static SlingHttpResponse getNodes(SlingClient client, String path, int expectedStatus) throws ClientException, IOException {
        String url = ADMIN_PREFIX_URL + "nodes.json" + path;
        return client.doGet(url, expectedStatus);
    }
}
