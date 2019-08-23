package com.peregrine.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Commonly used Constants in Peregrine
 *
 * Created by Andreas Schaefer on 6/19/17.
 */
public class PerConstants {

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
    public static final String COMPONENT_PRIMARY_TYPE = "per:Component";
    public static final String PAGE_PRIMARY_TYPE = "per:Page";
    public static final String PAGE_CONTENT_TYPE = "per:PageContent";
    public static final String ASSET_PRIMARY_TYPE = "per:Asset";
    public static final String ASSET_CONTENT_TYPE = "per:AssetContent";
    public static final String OBJECT_PRIMARY_TYPE = "per:Object";
    public static final String PER_REPLICATION = "per:Replication";
    public static final String PER_REPLICATED_BY = "per:ReplicatedBy";
    public static final String PER_REPLICATED = "per:Replicated";
    public static final String PER_REPLICATION_REF = "per:ReplicationRef";

    public static final String RENDITION_ACTION = "rendition.json";

    public static final String ORDER_BEFORE_TYPE = "before";
    public static final String ORDER_AFTER_TYPE = "after";
    public static final String ORDER_CHILD_TYPE = "child";

    public static final String ALLOWED_OBJECTS = "allowedObjects";
    public static final String CHILD_COUNT = "childCount";

    public static final String DISTRIBUTION_SUB_SERVICE = "peregrine-distribution-sub-service";
    public static final String RESOURCE_CHANGE_LISTENER = "metaproc-resource-change-listener";

    public static final String ECMA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final Locale ECMA_DATE_FORMAT_LOCALE = Locale.US;

    public static final String SLASH = "/";
    public static final String DASH = "-";

    public static final String JSON_MIME_TYPE = "application/json;charset=utf-8";
    public static final String HTML_MIME_TYPE = "text/html";
    public static final String DATA_JSON_EXTENSION = ".data.json";

    public static final String PNG_MIME_TYPE = "image/png";
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    public static final String WEBP_MIME_TYPE = "image/webp";
    //AS Does that exist?
    public static final String JPG_MIME_TYPE = "image/jpg";
    public static final String TEXT_MIME_TYPE = "plain/text";

    public static final String APPS_ROOT = "/apps";
    public static final String ASSETS_ROOT = "/content/assets";
    public static final String FELIBS_ROOT = "/etc/felibs";
    public static final String OBJECTS_ROOT = "/content/objects";
    public static final String SITES_ROOT = "/content/sites";
    public static final String TEMPLATES_ROOT = "/content/templates";

    public static final String COMPONENTS = "components";
    public static final String TEMPLATES = "templates";
    public static final String OBJECTS = "objects";

    public static final String COMPONENT = "component";
    public static final String PATH = "path";
    public static final String SITE = "site";
    public static final String NAME = "name";
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

    public static final String PAGE = "page";
    public static final String DROP = "drop";
    public static final String JACKSON = "jackson";
    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String HTML = "html";
    public static final String TXT = "txt";

    public static final String FROM_SITE_NAME = "fromSite";
    public static final String TO_SITE_NAME = "toSite";
    public static final String DEPENDENCIES = "dependencies";

    public static final String STANDALONE = "local";
    public static final String AUTHOR_RUN_MODE = "author";
    public static final String PUBLISH_RUN_MODE = "publish";

    public static final String PROTOCOL = "protocol";
    public static final String HOSTNAME = "hostname";
    public static final String CANONICAL_LINK_ELEMENT = "canonicalLink";
    public static final String HIDE_IN_NAVIGATION = "hideInNav";
    public static final String EXCLUDE_FROM_SITEMAP = "excludeFromSitemap";
    public static final String CHANGE_FREQUENCY = "changefreq";
    public static final String PRIORITY = "priority";
    public static final String DISALLOW_FROM_ROBOTS = "disallowFromRobots";

    public static final List<Pair<String, ?>> PAGE_PROPERTIES = Stream
        .of(Pair.of(PROTOCOL, "https://"),
            Pair.of(HOSTNAME, "www.metaproc.com"),
            Pair.of(CANONICAL_LINK_ELEMENT, "https://www.metaproc.com"),
            Pair.of(HIDE_IN_NAVIGATION, false),
            Pair.of(EXCLUDE_FROM_SITEMAP, false),
            Pair.of(CHANGE_FREQUENCY, "weekly"),
            Pair.of(PRIORITY, "0.5"),
            Pair.of(DISALLOW_FROM_ROBOTS, false))
        .collect(Collectors.toCollection(ArrayList::new));

    public static final String SITEMAP_SERVLET_PATH = "perapi/admin/sitemap";
    public static final String ROBOTS_SERVLET_PATH = "perapi/admin/robots";

    public static final String PAGE_RESOURCE_PREDICATE = "[jcr:primaryType] is 'per:Page' and [jcr:content/jcr:primaryType] is 'per:PageContent'";
    public static final String SITEMAP_RESOURCE_PREDICATE = "[sling:resourceType] is '" + SITEMAP_SERVLET_PATH + "'";
}

