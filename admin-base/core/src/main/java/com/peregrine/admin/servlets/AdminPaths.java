package com.peregrine.admin.servlets;

public interface AdminPaths {

    String API_PREFIX = "perapi/admin/";
    String RESOURCE_TYPE_ACCESS = API_PREFIX + "access";
    String RESOURCE_TYPE_COMPONENT_DEFINITION = API_PREFIX + "componentDefinition";
    String RESOURCE_TYPE_CONTENT = API_PREFIX + "content";
    String RESOURCE_TYPE_CREATION_FOLDER = API_PREFIX + "createFolder";
    String RESOURCE_TYPE_MANAGE_VERSIONS = API_PREFIX + "manageVersions";
    String RESOURCE_TYPE_CREATION_TENANT = API_PREFIX + "createTenant";
    String RESOURCE_TYPE_DELETE_TENANT = API_PREFIX + "deleteTenant";
    String RESOURCE_TYPE_CREATION_OBJECT = API_PREFIX + "createObject";
    String RESOURCE_TYPE_CREATION_OBJECT_DEFINITION = API_PREFIX + "createObjectDefinition";
    String RESOURCE_TYPE_CREATION_PAGE = API_PREFIX + "createPage";
    String RESOURCE_TYPE_CREATION_TEMPLATE = API_PREFIX + "createTemplate";
    String RESOURCE_TYPE_DELETE_NODE = API_PREFIX + "deleteNode";
    String RESOURCE_TYPE_DELETE_PAGE = API_PREFIX + "deletePage";
    String RESOURCE_TYPE_GET_OBJECT = API_PREFIX + "getObject";
    String RESOURCE_TYPE_LIST_REPLICATION_STATUS = API_PREFIX + "listReplicationStatus";
    String RESOURCE_TYPE_INSERT_NODE = API_PREFIX + "insertNodeAt";
    String RESOURCE_TYPE_LIST = API_PREFIX + "list";
    String RESOURCE_TYPE_MOVE_NODE = API_PREFIX + "moveNodeTo";
    String RESOURCE_TYPE_MOVE = API_PREFIX + "move";
    String RESOURCE_TYPE_RENAME = API_PREFIX + "rename";
    String RESOURCE_TYPE_COPY = API_PREFIX + "copy";
    String RESOURCE_TYPE_NODE = API_PREFIX + "node";
    String RESOURCE_TYPE_NODES = API_PREFIX + "nodes";
    String RESOURCE_TYPE_REF = API_PREFIX + "ref";
    String RESOURCE_TYPE_REF_BY = API_PREFIX + "refBy";
    String RESOURCE_TYPE_SEARCH = API_PREFIX + "search";
    String RESOURCE_TYPE_UPDATE_RESOURCE = API_PREFIX + "updateResource";
    String RESOURCE_TYPE_UPLOAD_FILES = API_PREFIX + "uploadFiles";
    String RESOURCE_TYPE_UPDATE_TENANT = API_PREFIX + "updateTenant";
    String RESOURCE_TYPE_LIST_TENANTS = API_PREFIX + "listTenants";
    String RESOURCE_TYPE_BACKUP_TENANT = API_PREFIX + "backupTenant";
    String RESOURCE_TYPE_DOWNLOAD_BACKUP_TENANT = API_PREFIX + "downloadBackupTenant";
    String RESOURCE_TYPE_UPLOAD_BACKUP_TENANT = API_PREFIX + "uploadBackupTenant";
    String RESOURCE_TYPE_RESTORE_TENANT = API_PREFIX + "restoreTenant";
    String RESOURCE_TYPE_LIST_RECYCLABLES = API_PREFIX + "listRecyclables";
    String RESOURCE_TYPE_LIST_VERSIONS = API_PREFIX + "listVersions";
    String RESOURCE_TYPE_RESTORE_RECYCLABLE = API_PREFIX + "restoreRecyclable";
    String RESOURCE_TYPE_USER_PREFERENCES = API_PREFIX + "userPreferences";
    String RESOURCE_TYPE_USER_HOMEPAGE = API_PREFIX + "userHomepage";
    String RESOURCE_TYPE_LIST_REPLICATION = API_PREFIX + "listRepl";
    String RESOURCE_TYPE_DO_REPLICATION = API_PREFIX + "repl";
    String RESOURCE_TYPE_TENANT_SETUP_REPLICATION = API_PREFIX + "tenantSetupReplication";
    String RESOURCE_TYPE_IS_TENANT_NAME_AVAILABLE = API_PREFIX + "tenants/name/available";

}
