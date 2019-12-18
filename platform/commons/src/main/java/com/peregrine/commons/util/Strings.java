package com.peregrine.commons.util;

import org.apache.commons.lang3.StringUtils;

public final class Strings {

    public static final String COLON = String.valueOf(Chars.COLON);
    public static final String EQ = String.valueOf(Chars.EQ);
    public static final String TAB = String.valueOf(Chars.TAB);
    public static final String _SCORE = String.valueOf(Chars._SCORE);

    private Strings() {
        throw new UnsupportedOperationException();
    }

    public static String firstNotBlank(final String... strings) {
        for (final String s : strings) {
            if (StringUtils.isNotBlank(s)) {
                return s;
            }
        }

        return null;
    }

    }
