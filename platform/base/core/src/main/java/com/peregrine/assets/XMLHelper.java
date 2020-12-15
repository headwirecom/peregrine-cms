package com.peregrine.assets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.replaceAll;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class XMLHelper {

    private static Logger logger = LoggerFactory.getLogger(XMLHelper.class);

    public static Document getXmlDocument(InputStream inputStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(inputStream);
        } catch (ParserConfigurationException | SAXException e) {
            logger.error("error creating xml doc from inputstream", e);
        }  catch (IOException e) {
            logger.error("error creating xml doc from inputstream", e);
        }
        return doc;
    }

    public static String normalizeSVGAlphanumeric(final String string) {
        var result = lowerCase(string);
        result = substringBeforeLast(result, "pt");
        return substringBeforeLast(result, ".");
    }

    public static boolean isExtendedAlphanumeric(final String string) {
        if (startsWith(string, "-")) {
            return isAlphanumeric(substringAfter(string, "-"));
        }
        return isAlphanumeric(string);
    }

    public static Rectangle parseRectangle(final String string) {
        if (isBlank(string)) {
            return null;
        }
        final var normalized = replaceAll(string, "\\s+", " ");
        final var parts = Arrays.stream(split(normalized, " "))
                .filter(XMLHelper::isExtendedAlphanumeric)
                .map(XMLHelper::normalizeSVGAlphanumeric)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (parts.size() != 4) {
            return null;
        }
        return new Rectangle(
                parts.remove(0),
                parts.remove(0),
                parts.remove(0),
                parts.remove(0)
        );
    }
}
