package com.peregrine.sitemap;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class XMLBuilderTest {

    private final XMLBuilder builder = new XMLBuilder();
    final Map<String, String> attributes = new HashMap<>();
    {
        attributes.put("a", "0");
    }

    @Test
    public void getAttributesLength() {
        final int size = XMLBuilder.getAttributesLength(attributes);
        assertEquals(6, size);
    }

    @Test
    public void getBasicElementLength() {
        final int size = XMLBuilder.getBasicElementLength("x");
        assertEquals(7, size);
    }

    @Test
    public void getBasicElementLength_withAttributes() {
        final int size = XMLBuilder.getBasicElementLength("x", attributes);
        assertEquals(13, size);
    }

    private String test(final String expected) {
        final String output = builder.toString();
        assertTrue(output.startsWith(XMLBuilder.XML_VERSION));
        assertEquals(expected, StringUtils.substringAfter(output, XMLBuilder.XML_VERSION));
        return output;
    }

    @Test
    public void emptyOutput() {
        test(StringUtils.EMPTY);
    }

    @Test
    public void emptyElement() {
        builder.startElement("x");
        test("<x />");
    }

    @Test
    public void startElementWithAttributes() {
        builder.startElement("x", attributes);
        builder.endElement();
        test("<x a=\"0\" />");
    }

    @Test
    public void addElementWithAttributesAndEmptyText() {
        builder.addElement("x", attributes, StringUtils.EMPTY);
        test("<x a=\"0\" />");
    }

    @Test
    public void addElementWithAttributesAndText() {
        builder.addElement("x", attributes, "text");
        test("<x a=\"0\">text</x>");
    }

    @Test
    public void addElementWithText() {
        builder.addElement("x", "text");
        test("<x>text</x>");
    }

    @Test
    public void complexXml() {
        builder.startElement("root");
        builder.startElement("container", attributes);
        builder.addElement("empty");
        builder.addElement("text", "text");
        builder.addElement("attributes", attributes);
        builder.addElement("text-attributes", attributes, "text");
        test("<root>"
                + "<container a=\"0\">"
                + "<empty />"
                + "<text>text</text>"
                + "<attributes a=\"0\" />"
                + "<text-attributes a=\"0\">text</text-attributes>"
                + "</container>"
                + "</root>");
    }

}