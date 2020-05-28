package com.peregrine.admin.servlets;

public interface AdminPaths {
    public static final String API_PREFIX = "perapi/admin/";
    public static final String RESOURCE_TYPE_ACCESS = API_PREFIX + "access";
    public static final String RESOURCE_TYPE_COMPONENT_DEFINITION = API_PREFIX + "componentDefinition";
    public static final String RESOURCE_TYPE_CONTENT = API_PREFIX + "content";
    public static final String RESOURCE_TYPE_CREATION_FOLDER = API_PREFIX + "createFolder";
    public static final String RESOURCE_TYPE_CREATION_TENANT = API_PREFIX + "createTenant";
    public static final String RESOURCE_TYPE_DELETE_TENANT = API_PREFIX + "deleteTenant";
    public static final String RESOURCE_TYPE_CREATION_OBJECT = API_PREFIX + "createObject";
    public static final String RESOURCE_TYPE_CREATION_PAGE = API_PREFIX + "createPage";
    public static final String RESOURCE_TYPE_CREATION_TEMPLATE = API_PREFIX + "createTemplate";
    public static final String RESOURCE_TYPE_DELETE_NODE = API_PREFIX + "deleteNode";
    public static final String RESOURCE_TYPE_DELETE_PAGE = API_PREFIX + "deletePage";
    public static final String RESOURCE_TYPE_GET_OBJECT = API_PREFIX + "getObject";
    public static final String RESOURCE_TYPE_INSERT_NODE = API_PREFIX + "insertNodeAt";
    public static final String RESOURCE_TYPE_LIST = API_PREFIX + "list";
    public static final String RESOURCE_TYPE_MOVE_NODE = API_PREFIX + "moveNodeTo";
    public static final String RESOURCE_TYPE_MOVE = API_PREFIX + "move";
    public static final String RESOURCE_TYPE_RENAME = API_PREFIX + "rename";
    public static final String RESOURCE_TYPE_COPY = API_PREFIX + "copy";
    public static final String RESOURCE_TYPE_NODE = API_PREFIX + "node";
    public static final String RESOURCE_TYPE_NODES = API_PREFIX + "nodes";
    public static final String RESOURCE_TYPE_REF = API_PREFIX + "ref";
    public static final String RESOURCE_TYPE_REF_BY = API_PREFIX + "refBy";
    public static final String RESOURCE_TYPE_SEARCH = API_PREFIX + "search";
    public static final String RESOURCE_TYPE_UPDATE_RESOURCE = API_PREFIX + "updateResource";
    public static final String RESOURCE_TYPE_UPLOAD_FILES = API_PREFIX + "uploadFiles";
    public static final String RESOURCE_TYPE_UPDATE_TENANT = API_PREFIX + "updateTenant";
    public static final String RESOURCE_TYPE_LIST_TENANTS = API_PREFIX + "listTenants";
}
