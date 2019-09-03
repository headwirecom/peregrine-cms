package com.peregrine.commons.util;

import com.peregrine.ResourceMock;
import org.junit.Test;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PerUtil_MatchingResourceChecker {

    private final ResourceMock source = new ResourceMock();
    private final ResourceMock target = new ResourceMock();
    private final ResourceMock resource = new ResourceMock();

    private final PerUtil.MatchingResourceChecker model;

    public PerUtil_MatchingResourceChecker() {
        String path = SLASH + "content" + SLASH;
        target.setPath(path + "target");
        path += "source";
        source.setPath(path);
        resource.setPath(path + SLASH + "resource");

        model = new PerUtil.MatchingResourceChecker(source, target);
    }

    @Test
    public void doAdd() {
        assertFalse(model.doAdd(resource));

        final ResourceMock targetResource = new ResourceMock();
        target.addChild(resource.getName(), targetResource);
        assertTrue(model.doAdd(resource));
    }
}