package com.peregrine.graphql.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.peregrine.graphql.schema.SchemaProvider;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static com.peregrine.commons.util.PerUtil.*;

@SuppressWarnings("rawtypes")
@Component(
    service = Servlet.class,
    name = "com.peregrine.graphql.servlets.GraphQLSchemaServlet",
    immediate = true,
    configurationPolicy= ConfigurationPolicy.REQUIRE,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Peregrine GraphQL Schema Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
    })
@Designate(ocd = GraphQLSchemaServlet.Config.class, factory=true)
public class GraphQLSchemaServlet extends SlingSafeMethodsServlet {

    public static final String GRAPHQL_SCHEMA_EXTENSION = "GQLschema";
    public static final String GRAPHQL_SCHEMA_BUILD_FAILURES = "schemaBuildFailures";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ObjectClassDefinition(
        name = "Peregrine GraphQL Schema Servlet",
        description = "Servlet that provides GraphQL schemas for Peregrine")
    public @interface Config {
        @AttributeDefinition(
            name = "Selectors",
            description="Standard Sling servlet property")
        String[] sling_servlet_selectors() default "";

        @AttributeDefinition(
            name = "Resource Types",
            description="Standard Sling servlet property")
        String[] sling_servlet_resourceTypes() default "graphql/query";

        @AttributeDefinition(
            name = "Methods",
            description="Standard Sling servlet property")
        String[] sling_servlet_methods() default {"GET", "POST"};

        @AttributeDefinition(
            name = "Extensions",
            description="Standard Sling servlet property")
        String[] sling_servlet_extensions() default {GRAPHQL_SCHEMA_EXTENSION, GRAPHQL_SCHEMA_BUILD_FAILURES};
    }


    private BundleContext bundleContext;

    @Reference
    private SchemaProvider schemaProvider;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (GRAPHQL_SCHEMA_BUILD_FAILURES.equals(request.getRequestPathInfo().getExtension())) {
            getSchemaErrors(request, response);
        } else {
            getSchema(request, response);
        }
    }

    void getSchema(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try {
            Resource endpoint = request.getResource();
            String schema = schemaProvider.getSchema(endpoint);
            response.setContentType("text/x-graphql-schema");
            PrintWriter writer = response.getWriter();
            writer.print(schema);
        } catch(Error e) {
            log.error("Obtained a Schema failed with Error", e);
        }
    }

    void getSchemaErrors(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
//        try {
//            final Iterable<SchemaError> schemaErrors = service.getSchemaErrors(request.getResource());
//            response.setContentType("application/json;charset=UTF-8");
//            response.getWriter().print(toJson(schemaErrors));
//        } catch (BuilderException ex) {
//            LOG.warn("Could not determine schema errors.", ex);
//            throw new IOException(ex);
//        }
    }
//
//    String toJson(final Iterable<SchemaError> schemaErrors) throws JsonProcessingException {
//        return this.objectMapper.writeValueAsString(schemaErrors);
//    }
//
//    boolean isSchemaErrorsFeatureEnabled(final ToggleRouter toggleRouter) {
//        try {
//            return toggleRouter.isEnabled(FEATURE_TOGGLE_SCHEMA_ERRORS);
//        } catch (final Exception ex) {
//            log.error("Error while trying to check feature toggle: " + ex.getMessage());
//            return false;
//        }
//    }
}
