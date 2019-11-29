package com.peregrine.mock;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Map;
import java.util.NoSuchElementException;

public final class NodeIteratorMock extends RangeIteratorMockBase implements NodeIterator {

    private final Map<String, ResourceMock> nodes;

    public NodeIteratorMock(final Map<String, ResourceMock> nodes) {
        super(nodes.keySet().iterator());
        this.nodes = nodes;
    }

    @Override
    public Node nextNode() {
        if (keys.hasNext()) {
            return nodes.get(keys.next()).getNode();
        } else {
            throw new NoSuchElementException("No More Nodes available");
        }
    }

    @Override
    public Object next() {
        return nextNode();
    }
}
