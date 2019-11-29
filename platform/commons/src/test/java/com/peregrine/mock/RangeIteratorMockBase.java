package com.peregrine.mock;

import org.apache.commons.lang3.NotImplementedException;

import javax.jcr.RangeIterator;
import java.util.Iterator;

public abstract class RangeIteratorMockBase implements RangeIterator {

    protected final Iterator<String> keys;

    public RangeIteratorMockBase(final Iterator<String> keys) {
        this.keys = keys;
    }

    @Override
    public boolean hasNext() {
        return keys.hasNext();
    }

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

}
