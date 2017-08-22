package com.peregrine.nodejs.servlet;

import java.util.Arrays;
import java.util.List;

/**
 * Created by schaefa on 4/3/17.
 */
public class SlingNodeConstants {
    public static final String LIST_PACKAGES = "/api/nodejs/list";
    public static final String INSTALL_PACKAGE = "/api/nodejs/package/install";
    public static final String REMOVE_PACKAGE = "/api/nodejs/package/remove";

    public static final String LIST_NAME = "name";
    public static final String LIST_DEPTH = "depth";
    public static final String LIST_TYPE = "type";
    public static final String LIST_SIZE = "size";

    public static final String LIST_TYPE_ALL = "all";
    public static final String LIST_TYPE_DEV = "dev";
    public static final String LIST_TYPE_PROD = "prod";
    public static final List<String> LIST_ALLOWED_TYPES = Arrays.asList(LIST_TYPE_ALL, LIST_TYPE_DEV, LIST_TYPE_PROD);

    public static final String PACKAGE_NAME = "name";
    public static final String PACKAGE_VERSION = "version";
}
