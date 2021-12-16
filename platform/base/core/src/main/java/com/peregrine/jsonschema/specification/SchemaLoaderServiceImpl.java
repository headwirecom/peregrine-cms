package com.peregrine.jsonschema.specification;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = {SchemaLoaderService.class},
    immediate = true,
    property = {
        SERVICE_VENDOR + "=" + "Peregrine CMS"
    }
)
public class SchemaLoaderServiceImpl
    implements SchemaLoaderService
{

    private Logger logger = LoggerFactory.getLogger(getClass());

    private WeakHashMap<String, SchemaInstance> schemas = new WeakHashMap<>();
    private SchemaLoader schemaLoader = new SchemaLoaderImpl();

    @Activate
    void activate() {
        logger.info("Schema Loader Service started");
    }

    @Deactivate
    void deactivate() {
        logger.info("Schema Loader Service stopped");
    }

    @Override
    public boolean isSchemaAvailable(ResourceResolver resourceResolver, String nodePath) {
        return schemaLoader.isSchema(resourceResolver, nodePath);
    }

    @Override
    public Schema getSchema(ResourceResolver resourceResolver, String nodePath) {
        Schema answer = null;
        Resource schemaResource = schemaLoader.getSchemaResource(resourceResolver, nodePath);
        if(schemaResource != null) {
            SchemaInstance schemaInstance = schemas.get(schemaResource.getPath());
            if(schemaInstance != null) {
                if(isSchemaCurrent(resourceResolver, schemaInstance)) {
                    answer = schemaInstance.getSchema();
                }
            }
            if (answer == null) {
                answer = schemaLoader.getSchema(schemaResource);
                if (answer != null) {
                    schemas.put(schemaResource.getPath(), new SchemaInstance(answer, schemaLoader.getSchemaTimestamp(schemaResource)));
                }
            }
        }
        return answer;
    }

    private boolean isSchemaCurrent(ResourceResolver resourceResolver, SchemaInstance schemaInstance) {
        boolean answer = true;
        List<String> schemaPaths = schemaInstance.getSchema().getReferences();
        for(String schemaPath: schemaPaths) {
            Resource resource = resourceResolver.getResource(schemaPath);
            if(resource != null) {
                Resource content = resource.getChild(JCR_CONTENT);
                if (content != null) {
                    Date timestamp = content.getValueMap().get(JCR_LAST_MODIFIED, Date.class);
                    if (!schemaInstance.isCurrent(timestamp)) {
                        answer = false;
                        break;
                    }
                }
            }
        }
        return answer;
    }

    private static class SchemaInstance {
        private Schema schema;
        private Date timestamp;

        public SchemaInstance(Schema schema, Date timestamp) {
            this.schema = schema;
            this.timestamp = timestamp;
        }

        public Schema getSchema() {
            return schema;
        }

        public boolean isCurrent(Date date) {
            return date == null || date.getTime() <= timestamp.getTime();
        }
    }
}
