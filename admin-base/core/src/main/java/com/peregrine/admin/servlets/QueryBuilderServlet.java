package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.osgi.service.component.annotations.Component;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * QueryBuilder Servlet for Apache Sling
 *
 * Implements functionality similar to AEM QueryBuilder API.
 * Supports predicates like:
 * - path: search path
 * - type: node type (jcr:primaryType)
 * - property: property name
 * - property.value: property value
 * - property.operation: equals, like, exists, unequals
 * - orderby: property to order by
 * - orderby.sort: asc or desc
 * - p.limit: result limit
 * - p.offset: result offset
 *
 * Example usage:
 * /bin/querybuilder.json?path=/content&type=nt:unstructured&property=jcr:title&property.value=Test&p.limit=10
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/querybuilder.json",
                "sling.servlet.methods=GET"
        }
)
public class QueryBuilderServlet extends AbstractBaseServlet {

    private static final long serialVersionUID = 1L;

    private static final String PARAM_PATH = "path";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_PROPERTY = "property";
    private static final String PARAM_PROPERTY_VALUE = "property.value";
    private static final String PARAM_PROPERTY_OPERATION = "property.operation";
    private static final String PARAM_ORDERBY = "orderby";
    private static final String PARAM_ORDERBY_SORT = "orderby.sort";
    private static final String PARAM_LIMIT = "p.limit";
    private static final String PARAM_OFFSET = "p.offset";
    private static final String PARAM_GUESS_TOTAL = "p.guessTotal";

    private static final String DEFAULT_PATH = "/";
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    @Override
    protected Response handleRequest(Request request) throws IOException, ServletException {


        JsonResponse response = new JsonResponse();
        try {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            if (session == null) {
                return new ErrorResponse().setErrorMessage("Unable to obtain JCR session");
            }

            // Parse predicates from request parameters
            QueryPredicates predicates = parsePredicates(request);

            // Build and execute query
            String queryString = buildQuery(predicates);
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);

            // Apply limit and offset
            if (predicates.limit > 0) {
                query.setLimit(predicates.limit);
            }
            if (predicates.offset > 0) {
                query.setOffset(predicates.offset);
            }

            QueryResult result = query.execute();
            NodeIterator nodes = result.getNodes();

            // Build JSON response
            response.writeAttribute("success", true);
            response.writeAttribute("results", Math.toIntExact(nodes.getSize()));

            response.writeArray("hits");
            long hitCount = 0;

            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                response.writeObject();
                response.writeAttribute("_path", node.getPath());

                // Add properties
                response.writeObject("properties");
                var properties = node.getProperties();
                while (properties.hasNext()) {
                    var property = properties.nextProperty();
                    if (property.isMultiple()) {
                        response.writeArray(property.getName());
                        Arrays.stream(property.getValues())
                                .forEach(v -> {
                                    try {
                                        switch (v.getType()) {
                                            case PropertyType.STRING:
                                                response.writeString(property.getString());
                                                break;
                                            }
                                    } catch (Exception e) {
                                    }
                                });
                        response.writeClose();
                    } else {
                        switch (property.getType()) {
                            case PropertyType.STRING:
                                response.writeAttribute(property.getName(), property.getString());
                                break;
                            case PropertyType.LONG:
                                response.writeAttribute(property.getName(), property.getLong());
                                break;
                            case PropertyType.DOUBLE:
                                response.writeAttribute(property.getName(), property.getDouble());
                                break;
                            case PropertyType.DECIMAL:
                                response.writeAttribute(property.getName(), property.getDecimal().doubleValue());
                                break;
                            case PropertyType.BOOLEAN:
                                response.writeAttribute(property.getName(), property.getBoolean());
                                break;
                            case PropertyType.PATH:
                                response.writeAttribute(property.getName(), property.getPath());
                                break;
                        }
                    }
                }
                response.writeClose();
                hitCount++;
                response.writeClose();
            }
            response.writeClose();
            response.writeAttribute("total", hitCount);

            // Add query info for debugging
            response.writeObject("query");
            response.writeAttribute("statement", queryString);
            response.writeAttribute("language", "JCR-SQL2");
            response.writeCloseAll();
            return response;
        } catch (Exception e) {
            return new ErrorResponse().setErrorMessage(e.getMessage());
        }
    }

    private QueryPredicates parsePredicates(Request request) {
        QueryPredicates predicates = new QueryPredicates();

        // Parse path
        predicates.path = request.getParameter(PARAM_PATH);
        if (predicates.path == null || predicates.path.isEmpty()) {
            predicates.path = DEFAULT_PATH;
        }

        // Parse type
        predicates.type = request.getParameter(PARAM_TYPE);

        // Parse property predicates (supports multiple)
        Map<String, String> paramMap = request.getParameters();
        predicates.properties = new ArrayList<>();

        // Handle single property predicate
        String property = request.getParameter(PARAM_PROPERTY);
        if (property != null && !property.isEmpty()) {
            PropertyPredicate pred = new PropertyPredicate();
            pred.name = property;
            pred.value = request.getParameter(PARAM_PROPERTY_VALUE);
            pred.operation = request.getParameter(PARAM_PROPERTY_OPERATION);
            if (pred.operation == null || pred.operation.isEmpty()) {
                pred.operation = "equals";
            }
            predicates.properties.add(pred);
        }

        // Handle numbered property predicates (1_property, 2_property, etc.)
        Set<String> propertyIndices = paramMap.keySet().stream()
                .filter(k -> k.matches("\\d+_property"))
                .map(k -> k.split("_")[0])
                .collect(Collectors.toSet());

        for (String index : propertyIndices) {
            PropertyPredicate pred = new PropertyPredicate();
            pred.name = request.getParameter(index + "_property");
            pred.value = request.getParameter(index + "_property.value");
            pred.operation = request.getParameter(index + "_property.operation");
            if (pred.operation == null || pred.operation.isEmpty()) {
                pred.operation = "equals";
            }
            predicates.properties.add(pred);
        }

        // Parse orderby
        predicates.orderBy = request.getParameter(PARAM_ORDERBY);
        predicates.orderBySort = request.getParameter(PARAM_ORDERBY_SORT);
        if (predicates.orderBySort == null || predicates.orderBySort.isEmpty()) {
            predicates.orderBySort = "asc";
        }

        // Parse limit and offset
        String limitStr = request.getParameter(PARAM_LIMIT);
        if (limitStr != null && !limitStr.isEmpty()) {
            try {
                predicates.limit = Math.min(Integer.parseInt(limitStr), MAX_LIMIT);
            } catch (NumberFormatException e) {
                predicates.limit = DEFAULT_LIMIT;
            }
        } else {
            predicates.limit = DEFAULT_LIMIT;
        }

        String offsetStr = request.getParameter(PARAM_OFFSET);
        if (offsetStr != null && !offsetStr.isEmpty()) {
            try {
                predicates.offset = Integer.parseInt(offsetStr);
            } catch (NumberFormatException e) {
                predicates.offset = 0;
            }
        }

        return predicates;
    }

    private String buildQuery(QueryPredicates predicates) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM [nt:base] AS node WHERE ");

        List<String> conditions = new ArrayList<>();

        // Path condition
        if (!predicates.path.equals("/")) {
            conditions.add(String.format("ISDESCENDANTNODE(node, '%s')", escapeSql(predicates.path)));
        }

        // Type condition
        if (predicates.type != null && !predicates.type.isEmpty()) {
            conditions.add(String.format("node.[jcr:primaryType] = '%s'", escapeSql(predicates.type)));
        }

        // Property conditions
        for (PropertyPredicate prop : predicates.properties) {
            if (prop.name == null || prop.name.isEmpty()) {
                continue;
            }

            String condition = buildPropertyCondition(prop);
            if (condition != null && !condition.isEmpty()) {
                conditions.add(condition);
            }
        }

        // Combine conditions
        if (conditions.isEmpty()) {
            query.append("node.[jcr:primaryType] IS NOT NULL");
        } else {
            query.append(String.join(" AND ", conditions));
        }

        // Order by
        if (predicates.orderBy != null && !predicates.orderBy.isEmpty()) {
            query.append(String.format(" ORDER BY node.[%s] %s",
                    escapeSql(predicates.orderBy),
                    predicates.orderBySort.equalsIgnoreCase("desc") ? "DESC" : "ASC"));
        }

        return query.toString();
    }

    private String buildPropertyCondition(PropertyPredicate prop) {
        String propName = String.format("node.[%s]", escapeSql(prop.name));

        switch (prop.operation.toLowerCase()) {
            case "exists":
                return propName + " IS NOT NULL";
            case "unequals":
            case "not":
                if (prop.value != null && !prop.value.isEmpty()) {
                    return String.format("%s <> '%s'", propName, escapeSql(prop.value));
                }
                return null;
            case "like":
                if (prop.value != null && !prop.value.isEmpty()) {
                    return String.format("%s LIKE '%%%s%%'", propName, escapeSql(prop.value));
                }
                return null;
            case "equals":
            default:
                if (prop.value != null && !prop.value.isEmpty()) {
                    return String.format("%s = '%s'", propName, escapeSql(prop.value));
                }
                return propName + " IS NOT NULL";
        }
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    // Helper classes
    private static class QueryPredicates {
        String path;
        String type;
        List<PropertyPredicate> properties;
        String orderBy;
        String orderBySort;
        int limit;
        int offset;
    }

    private static class PropertyPredicate {
        String name;
        String value;
        String operation;
    }
}
