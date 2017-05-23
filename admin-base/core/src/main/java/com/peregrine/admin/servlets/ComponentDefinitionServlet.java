package com.peregrine.admin.servlets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.*;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=component definition servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES +"=api/admin/componentDefinition"
        }
)
@SuppressWarnings("serial")
public class ComponentDefinitionServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ComponentDefinitionServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        Map<String, String> params = convertSuffixToParams(request);
        String path = params.get("path");
        ResourceResolver rr = request.getResourceResolver();
        Resource resource = rr.getResource(path);
        String componentPath = "/apps/"+resource.getValueMap().get("sling:resourceType", String.class);
        Resource component = rr.getResource(componentPath);
        Resource dialog = component.getChild("dialog.json");
        if(dialog == null) {
            dialog = getDialogFromSuperType(rr, component);
        }
        JsonFactory jf = new JsonFactory();
        JsonGenerator jg = jf.createGenerator(response.getWriter());
        jg.writeStartObject();
        jg.writeStringField("path", componentPath);
        jg.writeStringField("name", ServletHelper.componentPathToName(componentPath));
        if(dialog == null) {

        } else {
            jg.writeRaw(",\"model\": ");
            jg.writeRaw(ServletHelper.asString(dialog.adaptTo(InputStream.class)).toString());
        }
        jg.writeEndObject();
        jg.close();


    }

    private Resource getDialogFromSuperType(ResourceResolver rr, Resource resource) {
        String componentPath = resource.getValueMap().get("sling:resourceSuperType", String.class);
        if(componentPath != null) {
            if (!componentPath.startsWith("/apps")) {
                componentPath = "/apps/" + componentPath;
            }
            resource = rr.getResource(componentPath);
            Resource component = rr.getResource(componentPath);
            Resource dialog = component.getChild("dialog.json");
            if (dialog == null) {
                return getDialogFromSuperType(rr, component);
            } else {
                return dialog;
            }
        } else {
            return null;
        }
    }

}

