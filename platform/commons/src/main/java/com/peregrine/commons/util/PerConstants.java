package com.peregrine.commons.util;

import java.util.Locale;

/**
 * Created by schaefa on 6/19/17.
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
    public static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    public static final String SLING_RESOURCE_SUPER_TYPE = "sling:resourceSuperType";
    public static final String SLING_FOLDER = "sling:Folder";
    public static final String SLING_ORDERED_FOLDER = "sling:OrderedFolder";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_CREATED_BY = "jcr:createdBy";
    public static final String JCR_LAST_MODIFIED = "jcr:lastModified";
    public static final String JCR_LAST_MODIFIED_BY = "jcr:lastModifiedBy";
    public static final String JCR_TITLE = "jcr:title";
    public static final String PAGE_PRIMARY_TYPE = "per:Page";
    public static final String PAGE_CONTENT_TYPE = "per:PageContent";
    public static final String ASSET_PRIMARY_TYPE = "per:Asset";
    public static final String ASSET_CONTENT_TYPE = "per:AssetContent";
    public static final String OBJECT_PRIMARY_TYPE = "per:Object";
    public static final String PER_REPLICATION = "per:Replication";
    public static final String PER_REPLICATED_BY = "per:ReplicatedBy";
    public static final String PER_REPLICATED = "per:Replicated";
    public static final String PER_REPLICATION_REF = "per:ReplicationRef";

    public static final String ORDER_BEFORE_TYPE = "before";
    public static final String ORDER_AFTER_TYPE = "after";
    public static final String ORDER_CHILD_TYPE = "child";

    public static final String ALLOWED_OBJECTS = "allowedObjects";

    public static final String DISTRIBUTION_SUB_SERVICE = "peregrine-distribution-sub-service";

    public static final String ECMA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final Locale ECMA_DATE_FORMAT_LOCALE = Locale.US;

}
