package com.peregrine.admin.security;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;


public interface PackageValidator {
    /**
     * Checks if the package passed as an InputStream is safe to be installed.
     *
     * @param packageStream input stream that provides the content of the package. note that after this method returns,
     *        the input stream is closed in any case.
     * @return {@code true} if package was found to be safe to be installed, {@code false} otherwise
     * @throws IOException if an I/O error occurrs
     */
    @NotNull
    boolean isPackageSafe(InputStream packageStream) throws IOException;
}
