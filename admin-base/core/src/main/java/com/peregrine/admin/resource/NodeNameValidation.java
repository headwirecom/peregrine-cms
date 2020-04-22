package com.peregrine.admin.resource;

/**
 * Provides methods for validating name strings to make sure they meet JCR and peregrine
 * specifications.
 */
public interface NodeNameValidation {

    /**
     * Checks whether the provided name contains any characters that are not allowed in page names,
     * in addition to any that would otherwise already not be allowed.
     *
     * @param name the name to validate
     * @return true if the name does not contain any invalid characters or sequences; false otherwise
     */
    boolean isValidPageName(String name);

    /**
     * Checks whether the provided name contains any characters that are not allowed in site names,
     * in addition to any that would otherwise already not be allowed.
     *
     * @param name the name to validate
     * @return true if the name does not contain any invalid characters or sequences; false otherwise
     */
    boolean isValidSiteName(String name);

    /**
     * Checks whether the provided name contains any invalid characters or sequences based on
     * the JCR specification (and other known isssues not covered by the specs)
     *
     * @see <a href="https://jackrabbit.apache.org/oak/docs/constraints.html">JCR name constraints</a>
     * @param name the name to validate
     * @return true if the name does not contain any invalid characters or sequences; false otherwise
     */
    boolean isValidNodeName(String name);

}
