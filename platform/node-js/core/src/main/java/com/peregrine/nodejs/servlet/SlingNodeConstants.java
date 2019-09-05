package com.peregrine.nodejs.servlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contants for Sling Node JS Handling
 * Created by Andreas Schaefer on 4/3/17.
 */
public class SlingNodeConstants {
    /** Servlet Path for List Packages **/
    public static final String LIST_PACKAGES = "/perapi/nodejs/list";
    /** Servlet Path for Install Package **/
    public static final String INSTALL_PACKAGE = "/perapi/nodejs/package/install";
    /** Servlet Path for Remove Package **/
    public static final String REMOVE_PACKAGE = "/perapi/nodejs/package/remove";

    /** Package Name argument **/
    public static final String LIST_NAME = "name";
    /** Package Depth argument **/
    public static final String LIST_DEPTH = "depth";
    /** Package Type argument **/
    public static final String LIST_TYPE = "type";
    /** Package Size argument **/
    public static final String LIST_SIZE = "size";

    /** List Type ALL **/
    public static final String LIST_TYPE_ALL = "all";
    /** List Type DEV **/
    public static final String LIST_TYPE_DEV = "dev";
    /** List Type PROD **/
    public static final String LIST_TYPE_PROD = "prod";
    /** List of all allowed List Types **/
    public static final List<String> LIST_ALLOWED_TYPES = Collections.unmodifiableList(
        Arrays.asList(LIST_TYPE_ALL, LIST_TYPE_DEV, LIST_TYPE_PROD)
    );

    /** Package Name argument **/
    public static final String PACKAGE_NAME = "name";
    /** Package Version argument **/
    public static final String PACKAGE_VERSION = "version";
}
