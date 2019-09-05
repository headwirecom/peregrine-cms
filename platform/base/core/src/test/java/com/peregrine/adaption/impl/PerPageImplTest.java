package com.peregrine.adaption.impl;

import com.peregrine.adaption.PerPage;
import org.junit.Test;

import static com.peregrine.adaption.impl.PerTestUtil.countIterable;
import static com.peregrine.adaption.impl.PerTestUtil.createPage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Andreas Schaefer on 6/21/17.
 */
public class PerPageImplTest {

    @Test
    public void listChildren() throws Exception {
        PerPage root = createPage(null, "root");
        PerPage child = createPage(root, "child");
        PerPage child1 = createPage(child, "child-1");
        PerPage child2 = createPage(child, "child-2");
        assertEquals("Only expected one child of root", 1, countIterable(root.listChildren()));
        assertEquals("Only expected two grand children of child", 2, countIterable(child.listChildren()));
        assertEquals("Did not expected any children of grand child 1", 0, countIterable(child1.listChildren()));
        assertEquals("Did not expected any children of grand child 2", 0, countIterable(child2.listChildren()));
    }

    @Test
    public void traverseNext() throws Exception {
        PerPage root = createPage(null, "root");
        PerPage child1 = createPage(root, "child-1");
        PerPage child2 = createPage(root, "child-2");
        PerPage child11 = createPage(child1, "child-1-1");
        PerPage child12 = createPage(child1, "child-1-2");
        PerPage child13 = createPage(child1, "child-1-3");
        PerPage child111 = createPage(child11, "child-1-1-1");
        PerPage child112 = createPage(child11, "child-1-1-2");
        PerPage child21 = createPage(child2, "child-2-1");
        PerPage child22 = createPage(child2, "child-2-2");

        checkNext("Child-1 was not return as next of Root", child1, root);
        checkNext("Child-11 was not return as next of Child-1", child11, child1);
        checkNext("Child-111 was not return as next of Child-11", child111, child11);
        checkNext("Child-112 was not return as next of Child-111", child112, child111);
        checkNext("Child-12 was not return as next of Child-111", child12, child112);
        checkNext("Child-13 was not return as next of Child-12", child13, child12);
        checkNext("Child-2 was not return as next of Child-13", child2, child13);
        checkNext("Child-21 was not return as next of Child-2", child21, child2);
        checkNext("Child-22 was not return as next of Child-22", child22, child21);
        assertNull("Child-22 must not return a page", child22.getNext());
    }

    private void checkNext(String message, PerPage expected, PerPage start) {
        assertEquals(message, expected.getResource(), start.getNext().getResource());
    }

    @Test
    public void traversePrevious() throws Exception {
        PerPage root = createPage(null, "root");
        PerPage child1 = createPage(root, "child-1");
        PerPage child2 = createPage(root, "child-2");
        PerPage child11 = createPage(child1, "child-1-1");
        PerPage child12 = createPage(child1, "child-1-2");
        PerPage child13 = createPage(child1, "child-1-3");
        PerPage child111 = createPage(child11, "child-1-1-1");
        PerPage child112 = createPage(child11, "child-1-1-2");
        PerPage child21 = createPage(child2, "child-2-1");
        PerPage child22 = createPage(child2, "child-2-2");

        checkPrevious("Child-21 was not return as previous of Child-22", child21, child22);
        checkPrevious("Child-2 was not return as previous of Child-21", child2, child21);
        checkPrevious("Child-13 was not return as previous of Child-2", child13, child2);
        checkPrevious("Child-12 was not return as previous of Child-13", child12, child13);
        checkPrevious("Child-112 was not return as previous of Child-12", child112, child12);
        checkPrevious("Child-111 was not return as previous of Child-112", child111, child112);
        checkPrevious("Child-11 was not return as previous of Child-111", child11, child111);
        checkPrevious("Child-1 was not return as previous of Child-11", child1, child11);
        checkPrevious("root was not return as previous of Child-1", root, child1);
//        assertNull("Child-111 must not return a page", root.getPrevious());
    }

    private void checkPrevious(String message, PerPage expected, PerPage start) {
        assertEquals(message, expected.getResource(), start.getPrevious().getResource());
    }
}