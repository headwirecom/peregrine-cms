package com.peregrine.commons.test.mock;

import org.apache.commons.lang3.NotImplementedException;

import javax.jcr.PropertyIterator;

public abstract class PropertyIteratorMockBase implements PropertyIterator {

    @Override
    public void skip(long skipNum) {
        throw new NotImplementedException("Mock not implemented");
    }

    @Override
    public long getSize() {
        throw new NotImplementedException("Mock not implemented");
    }

    @Override
    public long getPosition() {
        throw new NotImplementedException("Mock not implemented");
    }

    @Override
    public final Object next() {
        return nextProperty();
    }
}
