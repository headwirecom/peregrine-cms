package com.peregrine.sitemap;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;

public final class XMLBuilder {

    public static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final int SINGLE_ATTRIBUTE_SYMBOLS_LENGTH = 4;
    private static final int ELEMENT_SYMBOLS_LENGTH = 5;

    private final StringBuilder builder = new StringBuilder(XML_VERSION);
    private final Stack<String> stack = new Stack<>();

    public static int getAttributesSizeInFile(final Map<String, String> attributes) {
        int result = XMLBuilder.XML_VERSION.length();
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
        return getBasicElementLength(name) + getAttributesSizeInFile(attributes);
    }

    public void startElement(final String name, final Map<String, String> attributes) {
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
    }

    public void startElement(final String name) {
        startElement(name, Collections.EMPTY_MAP);
    }

    public void endElement() {
        endElement(builder, stack.pop());
    }

    private void endElement(final StringBuilder builder, final String name) {
        builder.append("</");
        builder.append(name);
        builder.append(">");
    }

    public void addElement(final String name, final Map<String, String> attributes, final String text) {
        startElement(name, attributes);
        builder.append(text);
        endElement();
    }

    public void addElement(final String name, final String text) {
        addElement(name, Collections.EMPTY_MAP, text);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(builder);
        for (int i = stack.size() - 1; i >= 0 ; i--) {
            endElement(result, stack.get(i));
        }

        return result.toString();
    }

}
