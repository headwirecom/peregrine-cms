package com.peregrine.sitemap;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;

public final class XMLBuilder {

    public static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final int SINGLE_ATTRIBUTE_SYMBOLS_LENGTH = " =\"\"".length();
    private static final int ELEMENT_SYMBOLS_LENGTH = "<></>".length();

    private final StringBuilder builder = new StringBuilder(XML_VERSION);
    private final Stack<String> stack = new Stack<>();
    private boolean emptyElementInProgress = false;

    public static int getAttributesLength(final Map<String, String> attributes) {
        int result = 0;
        for (final Map.Entry<String, String> e : attributes.entrySet()) {
            result += e.getKey().length();
            result += e.getValue().length();
            result += SINGLE_ATTRIBUTE_SYMBOLS_LENGTH;
        }

        return result;
    }

    public static int getBasicElementLength(final String name) {
        return ELEMENT_SYMBOLS_LENGTH + 2 * name.length();
    }

    public static int getBasicElementLength(final String name, final Map<String, String> attributes) {
        return getBasicElementLength(name) + getAttributesLength(attributes);
    }

    public XMLBuilder startElement(final String name, final Map<String, String> attributes) {
        stack.push(name);
        builder.append("<");
        builder.append(name);
        for (final Map.Entry<String, String> e : attributes.entrySet()) {
            builder.append(" ");
            builder.append(e.getKey());
            builder.append("=\"");
            builder.append(e.getValue());
            builder.append("\"");
        }

        builder.append(">");
        emptyElementInProgress = true;
        return this;
    }

    public XMLBuilder startElement(final String name) {
        return startElement(name, Collections.emptyMap());
    }

    public XMLBuilder endElement() {
        final String name = stack.pop();
        if (emptyElementInProgress) {
            emptyElementInProgress = false;
            return endEmptyElement(builder);
        }

        return endElement(builder, name);
    }

    private XMLBuilder endElement(final StringBuilder builder, final String name) {
        builder.append("</");
        builder.append(name);
        builder.append(">");
        return this;
    }

    private XMLBuilder endEmptyElement(final StringBuilder builder) {
        final int length = builder.length();
        builder.replace(length - 1, length, " />");
        return this;
    }

    public XMLBuilder addElement(final String name, final Map<String, String> attributes, final String text) {
        startElement(name, attributes);
        if (StringUtils.isNotEmpty(text)) {
            builder.append(text);
            emptyElementInProgress = false;
        }

        return endElement();
    }

    public XMLBuilder addElement(final String name, final Map<String, String> attributes) {
        return addElement(name, attributes, null);
    }

    public XMLBuilder addElement(final String name, final String text) {
        return addElement(name, Collections.emptyMap(), text);
    }

    public XMLBuilder addElement(final String name) {
        return addElement(name, (String)null);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(builder);
        int start = stack.size() - 1;
        if (start >= 0 && emptyElementInProgress) {
            endEmptyElement(result);
            start--;
        }

        for (int i = start; i >= 0 ; i--) {
            endElement(result, stack.get(i));
        }

        return result.toString();
    }

}
