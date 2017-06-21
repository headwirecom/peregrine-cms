package com.peregrine.adaption.impl;

import com.peregrine.adaption.PerPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schaefa on 6/21/17.
 */
public class PerTestUtil {

    static Map<Resource, List<Resource>> childrenMap = new HashMap<>();

    public static Resource createResource(Resource parent, String name, String primaryType) {
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

    public static Resource createPageResource(Resource parent, String name) {
        return createResource(parent, name, PAGE_PRIMARY_TYPE);
    }

    public static PerPage createPage(PerPage parent, String name) {
        Resource pageResource =createPageResource(parent == null ? null : parent.getResource(), name);
        addRandomChildren(pageResource, 0, 2);
        Resource pageContent = createResource(pageResource, JCR_CONTENT, PAGE_CONTENT_TYPE);
        addRandomChildren(pageResource, 0, 2);
        Resource pageContentChild = createResource(pageContent, name + "-content-child", "test");

        return new PerPageImpl(pageResource);
    }

    public static void addRandomChildren(Resource parent, int min, int max) {
        int loop = (int) (Math.random() * (max + 1) + min);
        for(int i = 0; i < loop; i++) {
            createResource(parent, "random-child-" + i, "gugus-" + i);
        }
    }

}
