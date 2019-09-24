package com.peregrine.nodejs.servlet;

import com.peregrine.nodejs.npm.NpmExternalProcess;
import com.peregrine.nodejs.util.ObjectConverter;
import com.peregrine.process.ExternalProcessException;
import com.peregrine.process.ProcessContext;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.INSTALL_PACKAGE;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_ALLOWED_TYPES;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_DEPTH;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_NAME;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_PACKAGES;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_SIZE;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_TYPE;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_TYPE_ALL;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.PACKAGE_NAME;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.PACKAGE_VERSION;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.REMOVE_PACKAGE;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Rest API Servlet to serve the Sling Node API
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Sling Node Servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_PATHS + EQUAL + LIST_PACKAGES,
        SLING_SERVLET_PATHS + EQUAL + INSTALL_PACKAGE,
        SLING_SERVLET_PATHS + EQUAL + REMOVE_PACKAGE
    }
)
@SuppressWarnings("serial")
public class SlingNodeServiceServlet extends SlingAllMethodsServlet {

    public static final String USER_IN_NOT_LOGGED_IN = "User is either not logged in or does not have the necessary permissions to list packages";
    public static final String APPS_NODEJS_PERMISSION_LIST = "/apps/nodejs/permission/list";
    public static final String APPS_NODEJS_PERMISSION_MODIFY_OR_CANNOT_LIST = "/apps/nodejs/permission/modify";
    public static final String USER_IS_EITHER_NOT_LOGGED_IN_OR_CANNOT_MODIFY = "User is either not logged in or does not have the necessary permissions to modify packages";
    public static final String TYPE_IS_NOT_ACCEPTED = "Type is not accepted: ";
    public static final String SIZE_IS_NOT_A_NUMBER = "Size is not a number";
    public static final String FAILED_TO_OPEN_OUTPUT_FILE = "Failed to open output file";
    public static final String FAILED_TO_LIST_PACKAGES = "Failed to List Packages";
    public static final String LIST_PACKAGES_FAILED = "List Packages failed";
    public static final String PACKAGE_NAME_IS_REQUIRED_FOR_INSTALLING_A_PACKAGE = "Package Name is required for installing a Package";
    public static final String SUCCESSFULLY_INSTALLED_PACKAGE = "Successfully installed package: ";
    public static final String INSTALLATION_DID_NOT_SUCCEED = "Installation did not succeed";
    public static final String PACKAGE_NAME_IS_REQUIRED_FOR_REMOVING_A_PACKAGE = "Package Name is required for removing a package";
    public static final String PACKAGE_VERSION_IS_REQUIRED_FOR_REMOVING_A_PACKAGE = "Package Version is required for removing a package";
    public static final String SUCCESSFULLY_REMOVED_PACKAGE = "Successfully removed package: ";
    public static final String REMOVAL_DID_NOT_SUCCEED = "Removal did not succeed";

    private final Logger log = LoggerFactory.getLogger(SlingNodeServiceServlet.class);

    private static final String SUCCESS_MESSAGE =
        "{\n" +
        "    \"message\": \"%1$s\",\n" +
        "    \"output\": %2$s\n" +
        "}";

    private static final String ERROR_MESSAGE =
        "{\n" +
        "    \"code\": %1$s,\n" +
        "    \"message\": \"%2$s\",\n" +
        "    \"error\": %3$s\n" +
        "}";

    public static final int LOGIN_EXIT_CODE = 999;

    private static int FAILURE_CODE = HttpServletResponse.SC_BAD_REQUEST;

    @Reference
    private NpmExternalProcess npmExternalProcess;

    @Override
    protected void doGet(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response
    ) throws ServletException,
        IOException
    {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource permission = resourceResolver.resolve(request, APPS_NODEJS_PERMISSION_LIST);
        log.trace("Permission Resource: '{}'", permission);
        if(ResourceUtil.isNonExistingResource(permission)) {
            sendErrorMessage(response, LOGIN_EXIT_CODE, USER_IN_NOT_LOGGED_IN, HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            log.trace("Sling Node Servlet called");
            // Check the path to see what action is required
            if(request.getPathInfo().startsWith(LIST_PACKAGES)) {
                handleListPackages(request, response);
            }
        }
    }

    @Override
    protected void doPost(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response
    ) throws ServletException,
        IOException
    {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource permission = resourceResolver.resolve(request, APPS_NODEJS_PERMISSION_MODIFY_OR_CANNOT_LIST);
        log.trace("Permission Resource: '{}'", permission);
        if(ResourceUtil.isNonExistingResource(permission)) {
            sendErrorMessage(response, LOGIN_EXIT_CODE, USER_IS_EITHER_NOT_LOGGED_IN_OR_CANNOT_MODIFY, HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            log.trace("Sling Node Servlet called, POST");
            // Check the path to see what action is required
            if(request.getPathInfo().startsWith(INSTALL_PACKAGE)) {
                handleInstallPackage(request, response);
            } else if(request.getPathInfo().startsWith(REMOVE_PACKAGE)) {
                handleRemovePackage(request, response);
            }
        }
    }

    private void handleListPackages(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws IOException
    {
        String name = getParameter(request, LIST_NAME, String.class);
        if(name != null && name.isEmpty()) { name = null; }
        log.trace("Package Name: '{}'", name);

        int depth = -1;
        try {
            depth = getParameter(request, LIST_DEPTH, 0);
        } catch(NumberFormatException e) {
            createErrorResponse(response, "Depth is not a number", e);
            return;
        }
        log.trace("Package Depth: '{}'", depth);

        String type = getParameter(request, LIST_TYPE, String.class);
        if(type != null && !type.isEmpty()) {
            if(LIST_ALLOWED_TYPES.contains(type)) {
                if(LIST_TYPE_ALL.equals(type)) {
                    type = null;
                }
            } else {
                createErrorResponse(response, TYPE_IS_NOT_ACCEPTED + type);
                return;
            }
        }
        log.trace("Package Type: '{}'", type);

        int size = -1;
        try {
            size = getParameter(request, LIST_SIZE, -1);
        } catch(NumberFormatException e) {
            createErrorResponse(response, SIZE_IS_NOT_A_NUMBER, e);
            return;
        }
        log.trace("Package Size: '{}'", size);

        // Execute the action
        ProcessContext result = null;
        try {
            result = npmExternalProcess.listPackages(true, name, depth, type, size);
            if(result.getExitCode() == 0) {
                // Send the output back as is
                Reader reader = null;
                try {
                    response.setContentType(JSON_MIME_TYPE);
                    reader = result.getOutputReader();
                    IOUtils.copy(reader, response.getWriter());
                    log.trace("List Response: '{}'", result.getOutput());
                } catch(FileNotFoundException e) {
                    createErrorResponse(response, FAILED_TO_OPEN_OUTPUT_FILE, e);
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            } else {
                createErrorResponse(response, FAILED_TO_LIST_PACKAGES, result);
            }
            log.trace("Handle List Packages done");
        } catch(ExternalProcessException e) {
            createErrorResponse(response, LIST_PACKAGES_FAILED, e);
        } finally {
            if(result != null) { result.tearDown(); }
        }
    }

    private void handleInstallPackage(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws IOException
    {
        String name = getParameter(request, PACKAGE_NAME, String.class);
        if(name != null && name.isEmpty()) {
            createErrorResponse(response, PACKAGE_NAME_IS_REQUIRED_FOR_INSTALLING_A_PACKAGE);
            return;
        }
        log.trace("Package Name: '{}'", name);

        String version = getParameter(request, PACKAGE_VERSION, String.class);
        if(version != null && version.isEmpty()) { version = null; }
        log.trace("Package Version: '{}'", version);

        ProcessContext result = null;
        try {
            result = npmExternalProcess.installPackage(true, name, version);
            if(result.getExitCode() == 0) {
                createSuccessResponse(response, SUCCESSFULLY_INSTALLED_PACKAGE + name + (version != null ? "@" + version + "" : ""), result);
            } else {
                createErrorResponse(response, INSTALLATION_DID_NOT_SUCCEED, result);
            }
        } catch(ExternalProcessException e) {
            createErrorResponse(response, e);
        }
    }

    private void handleRemovePackage(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws IOException
    {
        String name = getParameter(request, PACKAGE_NAME, String.class);
        if(name != null && name.isEmpty()) {
            createErrorResponse(response, PACKAGE_NAME_IS_REQUIRED_FOR_REMOVING_A_PACKAGE);
            return;
        }
        log.trace("Package Name: '{}'", name);

        String version = getParameter(request, PACKAGE_VERSION, String.class);
        if(version != null && version.isEmpty()) {
            createErrorResponse(response, PACKAGE_VERSION_IS_REQUIRED_FOR_REMOVING_A_PACKAGE);
            return;
        }
        log.trace("Package Version: '{}'", version);

        ProcessContext result = null;
        try {
            result = npmExternalProcess.removePackage(true, name, version);
            if(result.getExitCode() == 0) {
                createSuccessResponse(response, SUCCESSFULLY_REMOVED_PACKAGE + name + (version != null ? "@" + version + "" : ""), result);
            } else {
                createErrorResponse(response, REMOVAL_DID_NOT_SUCCEED, result);
            }
        } catch(ExternalProcessException e) {
            createErrorResponse(response, e);
        }
    }

    private void createSuccessResponse(SlingHttpServletResponse response, String message, ProcessContext result)
        throws IOException
    {
        String output = result.getOutput();
        output = JSONObject.quote(output);
        String error = result.getError();
        error = JSONObject.quote(error);
        log.trace("Create Success Response: '{}', output: '{}', error: '{}'", new Object[] {message, output, error});
        log.trace("Output: '{}'",
            String.format(SUCCESS_MESSAGE,
                message,
                output
            )
        );
        StringReader reader = new StringReader(
            String.format(SUCCESS_MESSAGE,
                message,
                output
            )
        );
        // Send the output back as is
        try {
            response.setContentType(JSON_MIME_TYPE);
            IOUtils.copy(reader, response.getWriter());
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void createErrorResponse(SlingHttpServletResponse response, String message)
        throws IOException
    {
        log.error("Failed to handle Invocation: '{}'", message);
        sendErrorMessage(response, -1, message, "");
    }

    private void createErrorResponse(SlingHttpServletResponse response, String message, Exception e)
        throws IOException
    {
        log.error("Failed to handle NPM call: '{}'", message, e);
        String error = JSONObject.quote(
            e != null ?
                "Exception: " + e.getClass().getName() + ", message: " + e.getMessage() :
                ""
        );
        sendErrorMessage(response, -1, message, error);
    }

    private void createErrorResponse(SlingHttpServletResponse response, ExternalProcessException e)
        throws IOException
    {
        log.error("Failed to handle NPM call due to exception", e);
        ProcessContext result = e.getProcessContext();
        List<String> command = e.getCommand();
        String error = JSONObject.quote(
            ( command != null ?
                "Command: '" + command + "'\n" :
                ""
            ) +
            (
                result != null ?
                    result.getError() :
                    ""
            )

        );
        sendErrorMessage(response, -1, e.getMessage(), error);
    }

    private void createErrorResponse(SlingHttpServletResponse response, String message, ProcessContext result)
        throws IOException
    {
        log.error("NPM Command return an error code", result.getExitCode());
        String error = JSONObject.quote(result.getError());
        sendErrorMessage(response, result.getExitCode(), message, error);
    }

    private void sendErrorMessage(SlingHttpServletResponse response, int code, String message, String error)
        throws IOException
    {
        StringReader reader = new StringReader(
            String.format(ERROR_MESSAGE,
                code,
                message,
                error
            )
        );
        // Send the output back as is
        try {
            response.setContentType(JSON_MIME_TYPE);
            response.setStatus(FAILURE_CODE);
            IOUtils.copy(reader, response.getWriter());
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void sendErrorMessage(SlingHttpServletResponse response, int code, String message, int statusCode)
        throws IOException
    {
        StringReader reader = new StringReader(
            String.format(ERROR_MESSAGE,
                code,
                message,
                "\"\""
            )
        );
        // Send the output back as is
        try {
            response.setContentType(JSON_MIME_TYPE);
            response.setStatus(statusCode);
            IOUtils.copy(reader, response.getWriter());
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getParameter(SlingHttpServletRequest request, String name, T defaultValue) {
        T answer = getParameter(request, name, (Class<T>) defaultValue.getClass());

        if(answer == null) {
            answer = defaultValue;
        }

        return answer;
    }

    private <T> T getParameter(SlingHttpServletRequest request, String name, Class<T> type) {
        Object temp = request.getParameter(name);
        return ObjectConverter.convert(temp, type);
    }
}

