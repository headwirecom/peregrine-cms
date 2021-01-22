package com.peregrine.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public final class TextUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextUtils.class);

    public static final String PLACEHOLDER_NO_VALUE = "Place Holder: '%s' did not yield a value";
    public static final String PLACEHOLDER_UNMATCHED_SEPARATORS = "Place Holder String opened a Place Holder with '%s' but did not close it with: '%s'";
    public static final String PLACEHOLDER_START_TOKEN = "${";
    public static final String PLACEHOLDER_END_TOKEN = "}";

    public static String replacePlaceholders(final String source, final Function<String, String> replacer) {
        LOGGER.trace("System Properties: '{}'", System.getProperties());
        String answer = source;
        LOGGER.trace("Handle Place Holder: '{}'", source);
        while(true) {
            int startIndex = answer.indexOf(PLACEHOLDER_START_TOKEN);
            LOGGER.trace("Handle Place Holder, start index; '{}'", startIndex);
            if(startIndex >= 0) {
                int endIndex = answer.indexOf(PLACEHOLDER_END_TOKEN, startIndex);
                LOGGER.trace("Handle Place Holder, end index; '{}'", endIndex);
                if(endIndex >= 0) {
                    String placeHolderName = answer.substring(startIndex + PLACEHOLDER_START_TOKEN.length(), endIndex);
                    String value = System.getProperty(placeHolderName);
                    LOGGER.trace("Placeholder found: '{}', property value: '{}'", placeHolderName, value);
                    if(value == null) {
                        value = replacer.apply(placeHolderName);
                        LOGGER.trace("Placeholder found through bundle context: '{}', property value: '{}'", placeHolderName, value);
                    }
                    if(value != null) {
                        answer = answer.substring(0, startIndex) + value +
                                (answer.length() - 1 > endIndex ? answer.substring(endIndex + 1) : "");
                    } else {
                        throw new IllegalArgumentException(String.format(PLACEHOLDER_NO_VALUE, placeHolderName));
                    }
                } else {
                    throw new IllegalArgumentException(String.format(PLACEHOLDER_UNMATCHED_SEPARATORS, PLACEHOLDER_START_TOKEN, PLACEHOLDER_END_TOKEN));
                }
            } else {
                // Done -> exit
                break;
            }
        }
        LOGGER.trace("Place Holder handled, return: '{}'", answer);
        return answer;
    }

}
