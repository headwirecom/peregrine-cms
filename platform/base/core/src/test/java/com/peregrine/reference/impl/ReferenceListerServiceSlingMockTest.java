package com.peregrine.reference.impl;

import com.peregrine.reference.Reference;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import static  org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ReferenceListerServiceSlingMockTest {

    @Rule
    public final SlingContext context = new SlingContext();

    ReferenceListerService service = new ReferenceListerService();

    @Before
    public void init() {
        ReferenceListerService.Configuration configuration = mock(ReferenceListerService.Configuration.class);

        when(configuration.referencedByRoot()).thenReturn(new String[] { "/content" });
        when(configuration.referencePrefix()).thenReturn(new String[] { "/content" });
        service.activate(configuration);
    }

    @Test
    public void testReferencedByEmpty() {
    	assertTrue(service.getReferencedByList(null).isEmpty());
    }
    
    @Test
    public void testReferencedBy() {
        context.load().json("/referenceLister-tree.json", "/content");
        Resource index = context.resourceResolver().getResource("/content/tenant/pages/index");

        Resource spiedIndex = spy(index);
        when(spiedIndex.getResourceResolver()).thenAnswer(invocationOnMock -> {
            ResourceResolver rr = spy(context.resourceResolver());
            when(rr.findResources(anyString(), anyString()))
                    .thenReturn(context.resourceResolver().getResource("/content").getChildren().iterator());
            return rr;
        });

        List<Reference> referencedByList = service.getReferencedByList(spiedIndex);
        assertThat(referencedByList.size(), is(2));
        assertThat(referencedByList.get(0).getResource().getPath(), is("/content/res1"));
        assertThat(referencedByList.get(1).getResource().getPath(), is("/content/res3"));

    }

}
