package com.peregrine.commons.test.mock;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Map;

public final class NodeIteratorMock extends RangeIteratorMockBase implements NodeIterator {

    private final Map<String, ResourceMock> nodes;

    public NodeIteratorMock(final Map<String, ResourceMock> nodes) {
        super(nodes.keySet().iterator());
        this.nodes = nodes;
    }

    @Override
    public Node nextNode() {
        return nodes.get(keys.next()).getNode();
    }

    @Override
    public Object next() {
        return nextNode();
    }
}
