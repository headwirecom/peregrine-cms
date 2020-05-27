package com.peregrine.commons.util;

import com.peregrine.commons.Strings;

import java.util.Locale;

/**
 * Commonly used Constants in Peregrine
 *
 * Created by Andreas Schaefer on 6/19/17.
 */
public class PerConstants {
    public static final String ADMIN_USER = "admin";

    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_DATA = "jcr:data";
    public static final String NT_FOLDER = "nt:folder";
    public static final String NT_FILE = "nt:file";
    public static final String NT_RESOURCE = "nt:resource";
    public static final String NT_UNSTRUCTURED = "nt:unstructured";
    public static final String JCR_MIME_TYPE = "jcr:mimeType";
    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    public static final String JCR_UUID = "jcr:uuid";
    public static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    public static final String SLING_RESOURCE_SUPER_TYPE = "sling:resourceSuperType";
    public static final String SLING_FOLDER = "sling:Folder";
    public static final String SLING_ORDERED_FOLDER = "sling:OrderedFolder";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_CREATED_BY = "jcr:createdBy";
    public static final String JCR_LAST_MODIFIED = "jcr:lastModified";
    public static final String JCR_LAST_MODIFIED_BY = "jcr:lastModifiedBy";
    public static final String JCR_TITLE = "jcr:title";
    public static final String SITE_PRIMARY_TYPE = "per:Site";
    public static final String COMPONENT_PRIMARY_TYPE = "per:Component";
    public static final String PAGE_PRIMARY_TYPE = "per:Page";
    public static final String PAGE_CONTENT_TYPE = "per:PageContent";
    public static final String ASSET_PRIMARY_TYPE = "per:Asset";
    public static final String ASSET_CONTENT_TYPE = "per:AssetContent";
    public static final String OBJECT_PRIMARY_TYPE = "per:Object";
    public static final String OBJECT_DEFINITION_PRIMARY_TYPE = "per:ObjectDefinition";
    public static final String PER_REPLICATION = "per:Replication";
    public static final String PER_REPLICATED_BY = "per:ReplicatedBy";
    public static final String PER_REPLICATED = "per:Replicated";
    public static final String PER_REPLICATION_REF = "per:ReplicationRef";

    public static final String SLING_SERVLET_DEFAULT = "sling/servlet/default";

    public static final String UTF_8 = "utf-8";

    public static final String RENDITION_ACTION = "rendition.json";

    public static final String ORDER_BEFORE_TYPE = "before";
    public static final String ORDER_AFTER_TYPE = "after";
    public static final String ORDER_CHILD_TYPE = "child";

    public static final String ALLOWED_OBJECTS = "allowedObjects";
    public static final String CHILD_COUNT = "childCount";

    public static final String DISTRIBUTION_SUB_SERVICE = "peregrine-distribution-sub-service";

    public static final String ECMA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final Locale ECMA_DATE_FORMAT_LOCALE = Locale.US;

    public static final String SLASH = Strings.SLASH;
    public static final String DASH = Strings.DASH;

    public static final String HTML = "html";
    public static final String JSON = "json";

    public static final String DATA_JSON_EXTENSION = ".data.json";

    public static final String XML_MIME_TYPE = "application/xml";
    public static final String JSON_MIME_TYPE = "application/json;charset=utf-8";
    public static final String HTML_MIME_TYPE = "text/html";
    public static final String TEXT_MIME_TYPE = "plain/text";
    public static final String PNG_MIME_TYPE = "image/png";
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    public static final String WEBP_MIME_TYPE = "image/webp";
    public static final String JPG_MIME_TYPE = "image/jpg";

    public static final String PACKAGES = "packages";
    public static final String FELIBS = "felibs";
    public static final String ASSETS = "assets";
    public static final String OBJECTS = "objects";
    public static final String PAGES = "pages";
    public static final String TEMPLATES = "templates";
    public static final String COMPONENTS = "components";

    public static final String APPS_ROOT = "/apps";
    public static final String ETC_ROOT = "/etc";
    public static final String CONTENT_ROOT = "/content";

    public static final String TENANT = "${tenant}";

    public static final String PACKAGES_PATH = ETC_ROOT + SLASH + PACKAGES;
    public static final String FELIBS_ROOT = ETC_ROOT + SLASH + FELIBS;
    public static final String ASSETS_ROOT = CONTENT_ROOT + SLASH + TENANT + SLASH + ASSETS;
    public static final String OBJECTS_ROOT = CONTENT_ROOT + SLASH + TENANT + SLASH + OBJECTS;
    public static final String PAGES_ROOT = CONTENT_ROOT + SLASH + TENANT + SLASH + PAGES;
    public static final String TEMPLATES_ROOT = CONTENT_ROOT + SLASH + TENANT + SLASH + TEMPLATES;

    public static final String FOLDER = "folder";
    public static final String ASSET = "asset";
    public static final String OBJECT = "object";
    public static final String PAGE = "page";
    public static final String TEMPLATE = "template";
    public static final String COMPONENT = "component";
    public static final String SITE = "site";
    public static final String NODE = "node";
    public static final String RENDITION = "rendition";

    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final String DATA = "data";
    public static final String MODEL = "model";
    public static final String TYPE = "type";
    public static final String VARIATION = "variation";
    public static final String VARIATION_PATH = "variationPath";
    public static final String VARIATIONS = "__variations";
    public static final String STATUS = "status";
    public static final String TEMPLATE_PATH = "templatePath";
    public static final String SOURCE_PATH = "sourcePath";
    public static final String CREATED = "created";
    public static final String DELETED = "deleted";
    public static final String NODE_TYPE = "nodeType";
    public static final String PARENT_PATH = "parentPath";
    public static final String CONTENT = "content";
    public static final String TITLE = "title";
    public static final String TAGS = "tags";
    public static final String METAPROPERTIES = "metaproperties";
    public static final String UPDATED = "updated";
    public static final String OG_TAGS = "ogTags";
    public static final String COLOR_PALETTE = "colorPalette";
    public static final String DROP = "drop";
    public static final String JACKSON = "jackson";
    public static final String FROM_TENANT_NAME = "fromTenant";
    public static final String TO_TENANT_NAME = "toTenant";
    public static final String TO_TENANT_TITLE = "tenantTitle";
    public static final String TENANT_USER_PWD = "tenantUserPwd";
    public static final String DEPENDENCIES = "dependencies";
    public static final String INTERNAL = "internal";
    public static final String DOMAINS = "domains";

    public static final String EXCLUDE_FROM_SITEMAP = "excludeFromSitemap";
    public static final String CHANGE_FREQUENCY = "changefreq";
    public static final String PRIORITY = "priority";

    public static final String TENANT_GROUP_HOME = "/home/groups/tenants";
    public static final String TENANT_USER_HOME = "/home/users/tenants";
    public static final String ALL_TENANTS_GROUP_NAME = "all_tenants";
}

