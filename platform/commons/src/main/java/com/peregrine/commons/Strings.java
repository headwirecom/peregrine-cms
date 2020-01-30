package com.peregrine.commons;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public final class Strings {

    public static final String DOT = String.valueOf(Chars.DOT);
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

    public static String removeWhitespaces(final String string) {
        String result = StringUtils.replace(string, SPACE, EMPTY);
        result = StringUtils.replace(result, TAB, EMPTY);
        result = StringUtils.replace(result, LF, EMPTY);
        return result;
    }

}
