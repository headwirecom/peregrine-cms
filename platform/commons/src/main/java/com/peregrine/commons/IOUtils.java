package com.peregrine.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static java.util.Objects.isNull;

public final class IOUtils {

    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    public static final String FAILED_TO_DELETE_FILE = "Failed to delete file: '%s'";

    public static boolean deleteFileOrDirectory(final File file) {
        if (file.isDirectory()) {
            for (final File child: file.listFiles()) {
                if(!deleteFileOrDirectory(child)) {
                    logger.warn(String.format(FAILED_TO_DELETE_FILE, file.getAbsolutePath()));
                    return false;
                }
            }
        }

        return file.delete();
    }

    public static File createChildDirectory(final File parent, final String... name) {
        final int length = name.length;
        if (isNull(name) || length == 0) {
            return null;
        }

        int i = 0;
        File answer = new File(parent, name[i]);
        while (answer.exists() && !answer.isDirectory() && ++i < length) {
            answer = new File(parent, name[i]);
        }

        if ((answer.exists() && answer.isDirectory()) || answer.mkdir()) {
            return answer;
        }

        return null;
    }

}
