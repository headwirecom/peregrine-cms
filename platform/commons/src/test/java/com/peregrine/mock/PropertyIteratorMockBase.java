package com.peregrine.mock;

import javax.jcr.PropertyIterator;
import java.util.Iterator;

public abstract class PropertyIteratorMockBase extends RangeIteratorMockBase implements PropertyIterator {

    public PropertyIteratorMockBase(final Iterator<String> keys) {
        super(keys);
    }

    @Override
    public final Object next() {
        return nextProperty();
    }
}
