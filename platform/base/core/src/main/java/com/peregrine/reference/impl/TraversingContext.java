package com.peregrine.reference.impl;

import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.Resource;

import static java.util.Objects.isNull;

/**
 * Traversing context providing necessary flags as well as defining which resource are processed
 * It also makes sure that we don't end up in an endless loop with cyclic references
 **/
class TraversingContext implements PerUtil.ResourceChecker {
    private final PerUtil.ResourceChecker checker;
    private boolean transitive = false;
    private boolean deep = false;

    public TraversingContext(final PerUtil.ResourceChecker checker) {
        this.checker = isNull(checker) ? PerUtil.ADD_ALL_RESOURCE_CHECKER : checker;
    }

    public TraversingContext setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    public TraversingContext setDeep(boolean deep) {
        this.deep = deep;
        return this;
    }

    /**
     * @return True if references in references should be listed as well
     **/
    public boolean isTransitive() {
        return transitive;
    }

    /**
     * @return True if all children are traversed
     **/
    public boolean isDeep() {
        return deep;
    }

    @Override
    public boolean doAdd(Resource resource) {
        return checker.doAdd(resource);
    }

    @Override
    public boolean doAddChildren(Resource resource) {
        return checker.doAddChildren(resource);
    }

}
