package com.peregrine.nodetypes;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNodeType {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected abstract void registerNodeType(Session session) throws RepositoryException;
}
