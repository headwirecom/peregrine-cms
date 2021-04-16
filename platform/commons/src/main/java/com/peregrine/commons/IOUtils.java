package com.peregrine.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static java.util.Objects.isNull;

public final class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    public static final String FAILED_TO_DELETE_FILE = "Failed to delete file: '%s'";

    public static File createChildDirectory(final File parent, final String... namesToChooseFrom) {
        if (isNull(namesToChooseFrom) || namesToChooseFrom.length == 0) {
            return null;
        }

        int i = 0;
        File result = new File(parent, namesToChooseFrom[i]);
        while (result.exists() && !result.isDirectory() && ++i < namesToChooseFrom.length) {
            result = new File(parent, namesToChooseFrom[i]);
        }

        if ((result.exists() && result.isDirectory()) || result.mkdir()) {
            return result;
        }

        return null;
    }

    public static boolean deleteFileOrDirectory(final File file) {
        if (file.isDirectory()) {
            for (final File child: file.listFiles()) {
                if(!deleteFileOrDirectory(child)) {
                    LOGGER.warn(String.format(FAILED_TO_DELETE_FILE, file.getAbsolutePath()));
                    return false;
                }
            }
        }

        return file.delete();
    }

}
