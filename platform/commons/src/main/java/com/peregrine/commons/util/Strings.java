package com.peregrine.commons.util;

import org.apache.commons.lang3.StringUtils;

public final class Strings {

    public static String firstNotBlank(final String... strings) {
        for (final String s : strings) {
            if (StringUtils.isNotBlank(s)) {
                return s;
            }
        }

        return null;
    }

}
