package com.peregrine.commons.util;

import com.peregrine.ResourceMock;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PerUtil_MissingOrOutdatedResourceCheckerTest {

    private final ResourceMock source = new ResourceMock();
    private final ResourceMock target = new ResourceMock();
    private final ResourceMock resource = new ResourceMock();

    private final PerUtil.MissingOrOutdatedResourceChecker model;

    public PerUtil_MissingOrOutdatedResourceCheckerTest() {
        String path = SLASH + "content" + SLASH;
        target.setPath(path + "target");
        path += "source";
        source.setPath(path);
        resource.setPath(path + SLASH + "resource");

        model = new PerUtil.MissingOrOutdatedResourceChecker(source, target);
    }

    @Test
    public void doAdd() {
        assertTrue(model.doAdd(resource));

        final ResourceMock targetResource = new ResourceMock();
        target.addChild(resource.getName(), targetResource);

        assertFalse(model.doAdd(resource));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(2));
        resource.putProperty(PER_REPLICATED, calendar);
        assertFalse(model.doAdd(resource));

        calendar = Calendar.getInstance();
        calendar.setTime(new Date(3));
        targetResource.putProperty(PER_REPLICATED, calendar);
        assertFalse(model.doAdd(resource));

        calendar.setTime(new Date(1));
        assertTrue(model.doAdd(resource));
    }
}