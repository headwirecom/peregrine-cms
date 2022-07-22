package com.peregrine.graphql.data;

import com.peregrine.graphql.schema.SchemaProvider;
import com.peregrine.graphql.schema.model.QueryTypeEnum;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeFieldModel;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelType.Query;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.graphql.api.SelectedField;
import org.apache.sling.graphql.api.SelectionSet;
import org.apache.sling.graphql.api.SlingDataFetcher;
import org.apache.sling.graphql.api.SlingDataFetcherEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_PAGE_NAME;

@Component(
    service = SlingDataFetcher.class,
    property = { "name=sites/default"})
public class DefaultDataFetcher
    implements SlingDataFetcher<Object>
{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private SchemaProvider schemaProvider;

    @Reference
    private List<TypeDataFetcher> typeDataFetcherList;

//    private QueryField getFetcherField(TypeElement type, String fieldName) {
//        Iterable<Field> fields = type.getFields();
//        for (Field field : fields) {
//            if ((field instanceof QueryField) && field.getUniqueName().equals(fieldName)) {
//                return (QueryField) field;
//            }
//        }
//        throw new IllegalArgumentException("Invalid QueryType field: " + fieldName);
//    }

    @Override
    public Object get(SlingDataFetcherEnvironment env) throws Exception {
        Map<String, Object> answer = new HashMap<>();

        Resource tenantResource = env.getCurrentResource();
        Map<String, Object> arguments = env.getArguments();
        String source = env.getFetcherSource();
        if (source == null) {
            throw new IllegalArgumentException("No source provided to data fetcher.");
        }

        logger.debug("Creating fetcher for resource {}. source: {}",
            tenantResource.getPath(), source);

        SchemaModel schemaModel = schemaProvider.getSchemaModel(tenantResource);
        for(TypeDataFetcher typeDataFetcher: typeDataFetcherList) {
            List<Resource> typeResources = new ArrayList<>();
            Query typeQuery = typeDataFetcher.getQuery(source);
            TypeModel typeModel = typeDataFetcher.getTypeModel(typeQuery, schemaModel, source);
            if(typeModel != null) {
                typeResources.addAll(typeDataFetcher.obtainResource(tenantResource, typeModel, typeQuery, source, arguments));
                String selectedField = "item" + (typeQuery.isMultiple() ? "s" : "");
                SelectionSet selectionSet = env.getSelectionSet();
                List<Map<String, Object>> data = typeQuery.isMultiple() ? new ArrayList<>() : null;
                if(data != null) {
                    answer.put(selectedField, data);
                }
                SelectedField itemsField = selectionSet.get(selectedField);
                if (itemsField != null) {
                    List<SelectedField> selectedFieldList = itemsField.getSubSelectedFields();
                    for (Resource typeResource : typeResources) {
                        Map<String, Object> map = new HashMap<>();
                        for (SelectedField subField : selectedFieldList) {
                            String subFieldName = subField.getName();
                            TypeFieldModel subFieldType = typeModel.getField(subFieldName);
                            Object value = handleSubField(subField, subFieldType, typeResource);
                            map.put(subFieldName, value);
                        }
                        if(data != null) {
                            data.add(map);
                        } else {
                            answer.put(selectedField, map);
                        }
                    }
                }
            }
        }
        return answer;
    }

    private Object handleSubField(SelectedField field, TypeFieldModel fieldType, Resource resource) {
        Object answer = null;
        String fieldName = field.getName();
        if(PATH_FIELD_NAME.equals(fieldName)) {
            answer = resource.getPath();
        } else if(PATH_PAGE_NAME.equals(fieldName)) {
            Resource page = resource;
            while((page = page.getParent()) != null) {
                ValueMap properties = page.getValueMap();
                String primaryType = properties.get(JCR_PRIMARY_TYPE, String.class);
                if(PAGE_PRIMARY_TYPE.equals(primaryType)) {
                    break;
                }
            }
            answer = page != null ? page.getPath() : null;
        } else if(field.getSubSelectedFields().size() == 0) {
            ValueMap properties = resource.getValueMap();
            if (properties.containsKey(fieldName)) {
                answer = properties.get(fieldName);
            }
        } else {
            Map<String, Object> map = new HashMap<>();
            //TODO: Check if we need to search by Type Field Model's Original Name instead
            Resource childResource = resource.getChild(fieldName);
            TypeModel customType = (TypeModel) fieldType.getCustomType();
            if(fieldType.isArray()) {
                // Loop Over Children
                List<Object> list = new ArrayList<>();
                for(Resource grandChildResource: childResource.getChildren()) {
                    Map<String, Object> childMap = new HashMap<>();
                    for (SelectedField childField : field.getSubSelectedFields()) {
                        TypeFieldModel childFieldType = customType.getField(childField.getName());
                        Object value = handleSubField(childField, childFieldType, grandChildResource);
                        childMap.put(childField.getName(), value);
                    }
                    list.add(childMap);
                }
                answer = list;
            } else {
                for (SelectedField childField : field.getSubSelectedFields()) {
                    Object value = handleSubField(childField, fieldType, childResource);
                    map.put(childField.getName(), value);
                }
                answer = map;
            }
        }
        return answer;
    }

    private QueryTypeEnum getQueryType(String queryTypeName) {
        return Arrays.stream(QueryTypeEnum.values())
            .filter(e -> queryTypeName.endsWith(e.toString()))
            .findFirst()
            .orElse(QueryTypeEnum.Unknown);
    }
}
