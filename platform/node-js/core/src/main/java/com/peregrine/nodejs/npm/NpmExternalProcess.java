package com.peregrine.nodejs.npm;

import com.peregrine.process.ExternalProcessException;
import com.peregrine.process.ProcessContext;

/**
 * Node Package Manager service intefae to
 * list, install and remove packages
 *
 * Created by Andreas Schaefer on 4/4/17.
 */
public interface NpmExternalProcess {
    public static final String PROCESS_NAME_UNIX = "npm";
    public static final String PROCESS_NAME_WIN  = "npm.cmd";
    public static final String PROCESS_LIST = "ls";
    public static final String PROCESS_INSTALL = "install";
    public static final String PROCESS_REMOVE = "uninstall";

    public static final String PARAMETER_DEPTH = "--depth=";
    public static final String PARAMETER_TYPE_ONLY = "--only=";
    public static final String PARAMETER_JSON = "--json";

    /**
     * List all Packages that matches the given values
     * @param withJ2V8 If true then the the script wil be executed through J2V8
     * @param name If not null it will list all packages with the given name
     * @param depth How many levels deeps dependencies are listed
     * @param type Type of package to be listed
     * @param size Not used at the moment
     * @return Process Context that contains the result
     * @throws ExternalProcessException If the execution of the script failed
     */
    public ProcessContext listPackages(boolean withJ2V8, String name, int depth, String type, int size)
        throws ExternalProcessException;

    /**
     * Installs the requested package
     * @param withJ2V8 If the installation is done with J2V8
     * @param name Name of the package (cannot be empty)
     * @param version Version of the package if not null
     * @return Process Context with the result of the installation
     * @throws ExternalProcessException If the installation failed
     */
    public ProcessContext installPackage(boolean withJ2V8, String name, String version)
        throws ExternalProcessException;

    /**
     * Removes the given package
     * @param withJ2V8 If the removal is done with J2V8
     * @param name Name of the package (cannot be empty)
     * @param version Version of the package if not null
     * @return Process Context with the result of the removal
     * @throws ExternalProcessException If the removal failed
     */
    public ProcessContext removePackage(boolean withJ2V8, String name, String version)
        throws ExternalProcessException;
}
