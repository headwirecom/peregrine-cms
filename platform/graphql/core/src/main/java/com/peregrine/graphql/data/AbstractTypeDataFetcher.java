package com.peregrine.graphql.data;

import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelType;
import com.peregrine.graphql.schema.model.TypeModelType.Query;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;

public abstract class AbstractTypeDataFetcher
    implements TypeDataFetcher
{
    private TypeModelType typeModelType;

    public AbstractTypeDataFetcher(TypeModelType typeModelType) {
        this.typeModelType = typeModelType;
    }

    @Override
    public Query getQuery(String queryName) {
        Query answer = null;
        if(queryName != null) {
            for (Query query : typeModelType.getQueries()) {
                if (queryName.endsWith(query.getSuffix())) {
                    answer = query;
                    break;
                }
            }
        }
        return answer;
    }

    @Override
    public TypeModel getTypeModel(Query typeQuery, SchemaModel schemaModel, String queryName) {
        TypeModel answer = null;
        if(typeQuery != null && schemaModel != null && queryName != null) {
            String typeName = queryName.substring(0, queryName.length() - typeQuery.getSuffix().length());
            answer = schemaModel.getTypeByName(typeName);
        }
        return answer;
    }

    protected List<Resource> filterByArguments(List<Resource> resources, Map<String, Object> queryArguments, boolean onlyByPath) {
        List<Resource> answer = resources;
        String pathArgument = (String) queryArguments.get(PATH_FIELD_NAME);
        if(pathArgument != null && !pathArgument.isEmpty()) {
            answer = answer.stream()
                .filter(r -> r.getPath().startsWith(pathArgument))
                .collect(Collectors.toList());
        }
        if(!onlyByPath) {
            String fieldName = (String) queryArguments.get("fieldName");
            String fieldValue = (String) queryArguments.get("fieldValue");
            if(fieldName != null) {
                answer = answer.stream()
                    .filter(r -> {
                        ValueMap properties = r.getValueMap();
                        if (properties.containsKey(fieldName)) {
                            if(fieldValue == null || fieldValue.isEmpty()) {
                                return true;
                            } else {
                                return fieldValue.equals(properties.get(fieldName, String.class));
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            } else {
                answer.clear();
            }
        }
        return answer;
    }
}
