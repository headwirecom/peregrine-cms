package com.peregrine.adaption.impl;

import com.peregrine.adaption.PerPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schaefa on 6/20/17.
 */
public class PeregrineAdapterFactoryTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void adaptPageFromPageResource() throws Exception {
        Resource pageResource = createPageResource(null, "resource-1");
        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(pageResource, PerPage.class);
        assertNotNull("Page could not be adapted", page);
        checkPage("Adapted Page Resource is not what was excepted", pageResource, page);
    }

    private void checkPage(String message, Resource expected, PerPage found) {
        assertEquals(message, expected, found.getResource());
    }

    private void checkPage(String message, PerPage expected, PerPage found) {
        assertEquals(message, expected.getResource(), found.getResource());
    }

    @Test
    public void adaptPageFromContentTest() throws Exception {
        Resource pageResource = createPageResource(null, "resource-2");
        Resource jcrContent = createResource(pageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(jcrContent, PerPage.class);
        assertNotNull("Page Content could not be adapted", page);
        checkPage("Adapted Page Resource is not what was excepted", pageResource, page);
    }

    @Test
    public void adaptPageFromContentChildTest() throws Exception {
        Resource pageResource = createPageResource(null, "resource-3");
        Resource jcrContent = createResource(pageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);
        Resource jcrContentChild = createResource(jcrContent, "My Gugus", "gugus");

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(jcrContentChild, PerPage.class);
        assertNotNull("Page Content could not be adapted", page);
        checkPage("Adapted Page Resource is not what was excepted", pageResource, page);
    }

    @Test
    public void noAdaptNoPageContentChild() throws Exception {
        Resource pageResource = createPageResource(null, "resource-4");
        Resource pageNoContentResource = createResource(pageResource, "My Gugus", "gugus");

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(pageNoContentResource, PerPage.class);
        assertNull("Page Child should not be adapted as it is a no-content child", page);
    }

    @Test
    public void noAdaptNoPageChild() throws Exception {
        Resource notPageResource = createResource(null, "resource-5", "super:gugus");
        Resource jcrContent = createResource(notPageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);

        PeregrineAdapterFactory factory = new PeregrineAdapterFactory();
        PerPage page = factory.getAdapter(notPageResource, PerPage.class);
        assertNull("No Page should not be adapted as it has no Page Type", page);
        page = factory.getAdapter(jcrContent, PerPage.class);
        assertNull("No Page Content should not be adapted as it has no Page parent", page);
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

        checkPrevious("Child-2 was not return as previous of Root", child2, root);
        checkPrevious("Child-22 was not return as previous of Child-2", child22, child2);
        checkPrevious("Child-21 was not return as previous of Child-22", child21, child22);
        checkPrevious("Child-1 was not return as previous of Child-21", child1, child21);
        checkPrevious("Child-13 was not return as previous of Child-1", child13, child1);
        checkPrevious("Child-12 was not return as previous of Child-13", child12, child13);
        checkPrevious("Child-11 was not return as previous of Child-12", child11, child12);
        checkPrevious("Child-112 was not return as previous of Child-11", child112, child11);
        checkPrevious("Child-112 was not return as previous of Child-11", child111, child112);
        assertNull("Child-111 must not return a page", child111.getPrevious());
    }

    private void checkPrevious(String message, PerPage expected, PerPage start) {
        assertEquals(message, expected.getResource(), start.getPrevious().getResource());
    }

    Map<Resource, List<Resource>> childrenMap = new HashMap<>();

    private Resource createResource(Resource parent, String name, String primaryType) {
        Resource answer = mock(Resource.class, name);
        childrenMap.put(answer, new ArrayList<Resource>());
        when(answer.getName()).thenReturn(name);
        ValueMap properties = mock(ValueMap.class);
        when(properties.get(JCR_PRIMARY_TYPE, String.class)).thenReturn(primaryType);
        when(answer.getValueMap()).thenReturn(properties);
        when(answer.getChildren()).thenReturn(childrenMap.get(answer));
        if(parent != null) {
            when(answer.getParent()).thenReturn(parent);
            childrenMap.get(parent).add(answer);
        }
        return answer;
    }

    private Resource createPageResource(Resource parent, String name) {
        return createResource(parent, name, PAGE_PRIMARY_TYPE);
    }

    private PerPage createPage(PerPage parent, String name) {
        Resource pageResource =createPageResource(parent == null ? null : parent.getResource(), name);
        addRandomChildren(pageResource, 0, 2);
        Resource pageContent = createResource(pageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);
        addRandomChildren(pageResource, 0, 2);
        Resource pageContentChild = createResource(pageContent, name + "-content-child", "test");

        return new PerPageImpl(pageResource);
    }

    private void addRandomChildren(Resource parent, int min, int max) {
        int loop = (int) (Math.random() * (max + 1) + min);
        for(int i = 0; i < loop; i++) {
            createResource(parent, "random-child-" + i, "gugus-" + i);
        }
    }
}