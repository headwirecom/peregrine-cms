package com.peregrine.nodejs.npm;

import com.peregrine.nodejs.process.ExternalProcessException;
import com.peregrine.nodejs.process.ProcessContext;

/**
 * Created by Andreas Schaefer on 4/4/17.
 */
public interface NpmExternalProcess {
    public static final String PROCESS_NAME_UNIX = "npm";
    public static final String PROCESS_NAME_WIN  = "npm.cmd";
    public static final String PROCESS_LIST = "ls";
    public static final String PROCESS_INSTALL = "install";
    public static final String PROCESS_REMOVE = "uninstall";

    public static final String PARAMETER_DEPTH = "--depth=";
    public static final String PARAMETER_TYPE_ONLYT = "--only=";
    public static final String PARAMETER_JSON = "--json";

    public ProcessContext listPackages(boolean withJ2V8, String name, int depth, String type, int size)
        throws ExternalProcessException;

    public ProcessContext installPackage(boolean withJ2V8, String name, String version)
        throws ExternalProcessException;

    public ProcessContext removePackage(boolean withJ2V8, String name, String version)
        throws ExternalProcessException;
}
