package com.peregrine.jsonschema.specification;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import javax.jcr.Binary;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.SLASH;

public class SchemaLoaderImpl implements SchemaLoader {

    @Override
    public boolean isSchema(ResourceResolver resourceResolver, String nodePath) {
        boolean answer = false;
        Resource schemaResource = getSchemaResource(resourceResolver, nodePath);
        if(schemaResource != null) {
            Resource schemaContent = getSchemaContent(schemaResource);
            if (schemaContent != null) {
                answer = schemaContent.getValueMap().containsKey(JCR_DATA);
            }
        }
        return answer;
    }

    @Override
    public Resource getSchemaResource(ResourceResolver resourceResolver, String nodePath) {
        Resource answer = null;
        if(nodePath.startsWith(CONTENT_ROOT)) {
            if (nodePath.endsWith(SCHEMA_JSON_NODE_NAME)) {
                answer = resourceResolver.getResource(nodePath);
            } else {
                Resource parent = resourceResolver.getResource(nodePath);
                if (parent != null) {
                    answer = parent.getChild(SCHEMA_JSON_NODE_NAME);
                }
            }
        }
        return answer;
    }

    @Override
    public Date getSchemaTimestamp(Resource schemaResource) {
        Date answer = null;
        Resource schemaContent = getSchemaContent(schemaResource);
        if(schemaContent != null) {
            ValueMap properties = schemaContent.getValueMap();
            answer = properties.get(JCR_LAST_MODIFIED, Date.class);
        }
        return answer;
    }

    @Override
    public Schema getSchema(Resource schemaResource) {
        Schema answer = null;
        Resource schemaContent = getSchemaContent(schemaResource);
        if(schemaContent != null) {
            ValueMap properties = schemaContent.getValueMap();
            String content = properties.get(JCR_DATA, String.class);
            if(content != null) {
                try {
                    JCodeModel codeModel = new JCodeModel();
                    GenerationConfig config = new DefaultGenerationConfig() {
                        @Override
                        public boolean isGenerateBuilders() { // set config option by overriding method
                            return true;
                        }
                    };
                    List<String> references = new ArrayList<>(Arrays.asList(schemaResource.getPath()));
                    SchemaMapper mapper = new SchemaMapper(
                        new RuleFactory(
                            config,
                            new Jackson2Annotator(config),
                            new SchemaStore(
                                new PerSchemaContentResolver(schemaResource.getResourceResolver(), references)
                            )),
                        new SchemaGenerator()
                    );
                    mapper.generate(codeModel, "ClassName", "com.example", content);

                    java.io.File tempFile = Files.createTempDirectory("required").toFile();
                    codeModel.build(tempFile);

                    // Go though the Code Model and create our Schema Model from it
                    JPackage myPackage = codeModel._package("com.example");
                    JDefinedClass myClass = myPackage._getClass("ClassName");
                    SchemaImpl schema = new SchemaImpl(myClass.name(), "1.0", references);
                    Map<String, JFieldVar> myFields = myClass.fields();
                    handleSchemaFields(myFields, schema);
                    answer = schema;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return answer;
    }

    private void handleSchemaFields(Map<String, JFieldVar> myFields, SchemaImpl schema) {
        for (Entry<String, JFieldVar> entry : myFields.entrySet()) {
            String fieldName = entry.getKey();
            JFieldVar field = entry.getValue();
            JType type = field.type();
            // If the Type is of class JDefinedClass we have a $ref to another schema -> load the fields of that Type as properties
            if(type instanceof JDefinedClass) {
                JDefinedClass referenceType = (JDefinedClass) type;
                handleSchemaFields(referenceType.fields(), schema);
            }
            // Handle References to outside Schemas
            schema.addProperty(new PropertyImpl(fieldName, field.type().fullName()));
        }
    }
    private Resource getSchemaContent(Resource schemaResource) {
        Resource answer = null;
        if(schemaResource != null) {
            answer = schemaResource.getChild(JCR_CONTENT);
        }
        return answer;
    }
}
