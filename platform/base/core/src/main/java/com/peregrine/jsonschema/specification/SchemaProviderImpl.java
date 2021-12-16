package com.peregrine.jsonschema.specification;

import com.peregrine.jsonschema.specification.SchemaParser.SchemaException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.serviceusermapping.ServiceUserMapped;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.peregrine.jsonschema.Constants.EQUALS;
import static com.peregrine.jsonschema.Constants.JSON_SCHEMA_SERVICE_USER_NAME;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = {SchemaProvider.class},
    immediate = true,
    property = {
        SERVICE_VENDOR + "=" + "The Apache Software Foundation"
    }
)
public class SchemaProviderImpl
    implements SchemaProvider
{
    private static final String CONFIGURATION_PATH = "/apps/jsonschema3/schemas";
    private static final String NT_FILE = "nt:file";
    private static final String NT_RESOURCE = "nt:resource";
    private static final String JCR_CONTENT = "jcr:content";
    private static final String JCR_MIME_TYPE = "jcr:mimeType";
    private static final String JCR_DATE = "jcr:data";
    private static final String APPLICATION_JSON = "application/json";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    // Make sure that the Service User Mapping is available before obtaining the Service Resource Resolver
    @Reference(policyOption= ReferencePolicyOption.GREEDY, target="(" + ServiceUserMapped.SUBSERVICENAME + EQUALS + JSON_SCHEMA_SERVICE_USER_NAME + ")")
    private ServiceUserMapped serviceUserMapped;

    private ResourceResolver resourceResolver;

    private List<Schema> schemaList = new ArrayList<>();

    public void init(ResourceResolver resourceResolver) {
        if(this.resourceResolver == null) {
            this.resourceResolver = resourceResolver;
        }
        Resource schemaFolder = resourceResolver.getResource(CONFIGURATION_PATH);
        if (schemaFolder != null) {
            for (Resource schema : schemaFolder.getChildren()) {
                if (NT_FILE.equals(schema.getResourceType())) {
                    Resource content = schema.getChild(JCR_CONTENT);
                    if (content != null) {
                        if (NT_RESOURCE.equals(content.getResourceType())) {
                            ValueMap childProperties = content.getValueMap();
                            if (APPLICATION_JSON.equals(childProperties.get(JCR_MIME_TYPE, String.class))) {
                                String json = childProperties.get(JCR_DATE, String.class);
                                try {
                                    schemaList.add(SchemaParser.parseSchema(json));
                                } catch (SchemaException e) {
                                    logger.warn("Failed to parse schema: '{}'", schema);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public SchemaProviderImpl() {
    }

    @Activate
    void activate() {
        logger.info("activate called");
//        ResourceResolver resourceResolver;
        try (
            ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(
                new HashMap<String, Object>() {{ put(ResourceResolverFactory.SUBSERVICE, JSON_SCHEMA_SERVICE_USER_NAME); }}
            );
        ) {
            logger.info("Obtained Resource Resolver: '{}'", resourceResolver);
            init(resourceResolver);
        } catch(LoginException e) {
            logger.error("Failed to obtain Resource Resolver", e);
        }
    }

    @Modified
    void modified() {
        logger.info("modified called");
    }

    @Override
    public Schema getSchemaNameById(String schemaId) {
        return schemaList.stream().filter(s -> s.getId().equals(schemaId)).findFirst().orElse(null);
    }

    @Override
    public List<Schema> getSchemas() {
        return schemaList;
    }
}
