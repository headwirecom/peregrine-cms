package com.peregrine.admin.servlets;

public interface AdminPaths {
    String API_PREFIX = "perapi/admin/";
    String RESOURCE_TYPE_ACCESS = API_PREFIX + "access";
    String RESOURCE_TYPE_COMPONENT_DEFINITION = API_PREFIX + "componentDefinition";
    String RESOURCE_TYPE_CONTENT = API_PREFIX + "content";
    String RESOURCE_TYPE_CREATION_FOLDER = API_PREFIX + "createFolder";
    String RESOURCE_TYPE_CREATION_OBJECT = API_PREFIX + "createObject";
    String RESOURCE_TYPE_CREATION_PAGE = API_PREFIX + "createPage";
    String RESOURCE_TYPE_CREATION_SITE = API_PREFIX + "createSite";
    String RESOURCE_TYPE_CREATION_TEMPLATE = API_PREFIX + "createTemplate";
    String RESOURCE_TYPE_DELETE_NODE = API_PREFIX + "deleteNode";
    String RESOURCE_TYPE_DELETE_PAGE = API_PREFIX + "deletePage";
    String RESOURCE_TYPE_DELETE_SITE = API_PREFIX + "deleteSite";
    String RESOURCE_TYPE_GET_OBJECT = API_PREFIX + "getObject";
    String RESOURCE_TYPE_INSERT_NODE = API_PREFIX + "insertNodeAt";
    String RESOURCE_TYPE_LIST = API_PREFIX + "list";
    String RESOURCE_TYPE_LIST_TENANTS = API_PREFIX + "listTenants";
    String RESOURCE_TYPE_MOVE_NODE = API_PREFIX + "moveNodeTo";
    String RESOURCE_TYPE_MOVE = API_PREFIX + "move";
    String RESOURCE_TYPE_RENAME = API_PREFIX + "rename";
    String RESOURCE_TYPE_NODE = API_PREFIX + "node";
    String RESOURCE_TYPE_NODES = API_PREFIX + "nodes";
    String RESOURCE_TYPE_REF = API_PREFIX + "ref";
    String RESOURCE_TYPE_REF_BY = API_PREFIX + "refBy";
    String RESOURCE_TYPE_SEARCH = API_PREFIX + "search";
    String RESOURCE_TYPE_UPDATE_RESOURCE = API_PREFIX + "updateResource";
    String RESOURCE_TYPE_UPDATE_SITE = API_PREFIX + "updateSite";
    String RESOURCE_TYPE_UPLOAD_FILES = API_PREFIX + "uploadFiles";
}
