package com.peregrine.admin.resource;

import com.peregrine.admin.resource.ReferenceListerService.Node;
import com.peregrine.admin.resource.ReferenceListerService.Tree;
import com.peregrine.commons.test.AbstractTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TreeTest
    extends AbstractTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testStringFormat() {
        String format = "Test me with: '%s'";
        String value = "ABC";
        String expected = "Test me with: '" + value + "'";
        String actual = String.format(format, "ABC");
        logger.info("String after formatting: '{}'", actual);
        assertEquals("String Formatted String is not what is expected", expected, actual);
    }

    /** Tests a simple Tree created through creating nodes **/
    @Test
    public void testSimpleTreeCreation() {
        Tree tree = new Tree();
        Node child = new Node(tree, "a");
        Node grandChild = new Node(child, "b");
        logger.info("Tree 1: '{}'", tree);
        assertTrue("Grand Child not found", tree.contains("/a/b"));

        Node grandChild2 = new Node(child, "b2");
        logger.info("Tree 2: '{}'", tree);
        assertTrue("Grand Child (2) not found", tree.contains("/a/b2"));
    }

    /** Test a Tree using the JCR Resource Path **/
    @Test
    public void testTreByPath() {
        Tree tree = new Tree().addChildByPath("/a/b/c");
        logger.info("Tree 1: '{}'", tree);
        assertTrue("Initial Tree not found", tree.contains("/a/b/c"));

        tree.addChildByPath("/a/b2/c2");
        logger.info("Tree 2: '{}'", tree);
        assertTrue("Added 2nd level child not found", tree.contains("/a/b2/c2"));

        tree.addChildByPath("/a2/b/c");
        logger.info("Tree 3: '{}'", tree);
        assertTrue("Added 1st level child not found", tree.contains("/a2/b/c"));

        tree.addChildByPath("/a2/b/d/e/f");
        logger.info("Tree 4: '{}'", tree);
        assertTrue("Added 3rd level+ child not found", tree.contains("/a2/b/d/e/f"));
    }
}