package com.peregrine.admin.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandlerService;
import com.peregrine.admin.resource.NodeNameValidationService;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.RedirectResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.servlets.AbstractBaseServlet.TextResponse;
import com.peregrine.commons.test.AbstractTest;
import com.peregrine.commons.test.mock.PeregrineRequestMock;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.rendition.BaseResourceHandler;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.factory.ModelFactory;
import org.mockito.internal.util.reflection.Whitebox;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.servlets.AbstractBaseServlet.MESSAGE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerConstants.TEMPLATE_PATH;
import static com.peregrine.commons.util.PerConstants.TEXT_MIME_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractServletTest
    extends AbstractTest
{
    PeregrineRequestMock mockRequest;
    ResourceResolver mockResourceResolver;
    ResourceMock mockParentResource;
    Node mockParentNode;
    Node mockNewNode;
    Node mockNewContentNode;
    Node mockNewSiteNode;
    ResourceMock mockTemplateResource;
    ResourceMock mockNewResource;
    ModelFactory mockModelFactory;
    AdminResourceHandler adminResourceHandler;

    void setupCreation(String requestPath, String...parameters) throws RepositoryException {
        mockRequest = PeregrineRequestMock.createInstance(requestPath, parameters);

        // Create Parent Resource and Node
        mockResourceResolver = mockRequest.getResourceResolver();
        String path = mockRequest.getParameter(PATH);
        mockParentResource = new ResourceMock("Parent")
            .setPath(path);
        when(mockResourceResolver.getResource(eq(path))).thenReturn(mockParentResource);
        mockParentNode = mockParentResource.getNode();

        // Create Template Resource
        String templatePath = mockRequest.getParameter(TEMPLATE_PATH);
        if(templatePath != null) {
            mockTemplateResource = new ResourceMock()
                .setPath(templatePath + "/" + JCR_CONTENT)
                .putProperty(SLING_RESOURCE_TYPE, "test/me/later");
            when(mockResourceResolver.getResource(templatePath + "/" + JCR_CONTENT)).thenReturn(mockTemplateResource);
        }

        // Create new Object Node and Resource to be returned on creation
        String newResourcePath = path + "/" + mockRequest.getParameter(NAME);
        mockNewNode = mock(Node.class, "New Node");
        when(mockNewNode.getPath()).thenReturn(newResourcePath);
        mockNewResource = new ResourceMock()
            .setPath(newResourcePath)
            .setParent(mockParentResource);
        when(mockResourceResolver.getResource(eq(newResourcePath))).thenReturn(mockNewResource);
    }

    void setupDeletion(String requestPath, String...parameters) throws RepositoryException {
        mockRequest = PeregrineRequestMock.createInstance(requestPath, parameters);

        // Create Parent Resource and Node
        String path = mockRequest.getParameter(PATH);
        int index = path.lastIndexOf('/');
        String parentPath = path.substring(0, index);
        mockResourceResolver = mockRequest.getResourceResolver();
        mockParentResource = new ResourceMock("Parent")
            .setPath(parentPath);
        when(mockResourceResolver.getResource(eq(parentPath))).thenReturn(mockParentResource);
        mockParentNode = mockParentResource.getNode();

        // Create Template Resource
        String templatePath = mockRequest.getParameter(TEMPLATE_PATH);
        if(templatePath != null) {
            mockTemplateResource = new ResourceMock()
                .setPath(templatePath + "/" + JCR_CONTENT)
                .putProperty(SLING_RESOURCE_TYPE, "test/me/later");
            when(mockResourceResolver.getResource(templatePath + "/" + JCR_CONTENT)).thenReturn(mockTemplateResource);
        }

        // Create new Object Node and Resource to be returned on creation
        mockNewNode = mock(Node.class, "Old Node");
        when(mockNewNode.getPath()).thenReturn(path);
        mockNewResource = new ResourceMock()
            .setPath(path)
            .setParent(mockParentResource);
        mockParentResource.addChild(mockNewResource);
        when(mockResourceResolver.getResource(eq(path))).thenReturn(mockNewResource);
    }

    void setupPage() throws RepositoryException {
        mockNewContentNode = mock(Node.class, "New Page Content");
        when(mockNewNode.addNode(eq(JCR_CONTENT))).thenReturn(mockNewContentNode);
    }

    void setupSite(String newSiteName) throws RepositoryException, PersistenceException {
        when(mockParentNode.addNode(eq(newSiteName), eq(PAGE_PRIMARY_TYPE))).thenReturn(mockNewNode);
        mockParentResource.addChild(mockNewResource);
        mockNewResource.putProperty(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        mockNewResource.setResourceResolver(mockResourceResolver);
        ResourceMock answer = new ResourceMock()
            .setParent(mockParentResource)
            .setPath(mockParentResource.getPath() + "/" + newSiteName);
        when(mockResourceResolver.create(eq(mockParentResource), eq(newSiteName), any(Map.class))).thenReturn(answer);
        Node jcrContentNode = mock(Node.class, "New Site Content Node");
        when(mockNewNode.addNode(eq(JCR_CONTENT))).thenReturn(jcrContentNode);
        Session mockSession = mock(Session.class);
        when(mockNewNode.getSession()).thenReturn(mockSession);
        // For now we just return false for Component to exist
        when(mockSession.itemExists(anyString())).thenReturn(false);
    }

    void setupDeletion(String type) {
        mockNewResource.putProperty(JCR_PRIMARY_TYPE, type);
    }

    void setupServlet(Object servlet) {
        try {
            adminResourceHandler = new AdminResourceHandlerService();
            Whitebox.setInternalState(adminResourceHandler, "nodeNameValidation", new NodeNameValidationService());
            Whitebox.setInternalState(servlet, "resourceManagement", adminResourceHandler);
            BaseResourceHandler mockBaseResourceHandler = mock(BaseResourceHandler.class);
            Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", mockBaseResourceHandler);
        } catch(RuntimeException e) {}
        mockModelFactory = mock(ModelFactory.class);
        try {
            Whitebox.setInternalState(servlet, "modelFactory", mockModelFactory);
        } catch(RuntimeException e) {}
    }

    void checkJsonResponse(Response response, String...fileValuePairs) throws IOException {
        assertNotNull("No Response from Servlet", response);
        assertEquals("Response Type is not of type Json Response", JsonResponse.class.getName(), response.getClass().getName());
        assertFalse("Response Type is must not be of type Error Response", response instanceof ErrorResponse);
        JsonNode node = new ObjectMapper().readTree(response.getContent());
        int pairs = fileValuePairs.length / 2;
        for(int i = 0; i < pairs; i++) {
            String key = fileValuePairs[2*i];
            String value = fileValuePairs[2*i + 1];
            JsonNode childNode = node.get(key);
            assertNotNull("Node Field not found", childNode);
            assertEquals("Wrong Field Value", value, childNode.asText());
        }
    }

    void checkErrorResponse(Response response, String expectedErrorResponse) throws IOException {
        assertNotNull("No Response from Servlet", response);
        assertEquals("Response Type is not of type Error Response", ErrorResponse.class.getName(), response.getClass().getName());
        JsonNode node = new ObjectMapper().readTree(response.getContent());
        String message = node.get(MESSAGE).asText();
        assertEquals("Wrong Error Response", expectedErrorResponse, message);
    }

    void checkTextResponse(Response response, String expectedText, String expectedMimeType) {
        assertNotNull("No Response from Servlet", response);
        assertEquals("Response Type is not of type Text Response", TextResponse.class.getName(), response.getClass().getName());
        TextResponse textResponse = (TextResponse) response;
        assertEquals("Wrong Mime Type", expectedMimeType, textResponse.getMimeType());
        String text = textResponse.getContent();
        assertEquals("Wrong Text Response", expectedText, text);
    }

    void checkRedirectResponse(Response response, String expectedRedirectToPath) {
        assertNotNull("No Response from Servlet", response);
        assertEquals("Response Type is not of type Redirect Response", RedirectResponse.class.getName(), response.getClass().getName());
        RedirectResponse redirectResponse = (RedirectResponse) response;
        String redirectTo = (String) Whitebox.getInternalState(redirectResponse, "redirectTo");
        assertEquals("Wrong Redirect Path", expectedRedirectToPath, redirectTo);
    }
}
