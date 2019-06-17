package com.peregrine.nodetypes;

import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.PEREGRINE_NAMESPACE_PREFIX;
import static com.peregrine.commons.util.PerConstants.PEREGRINE_NAMESPACE_URI;

import java.util.Optional;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNodeType {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  public abstract Session getSession();

  public abstract NodeTypeManager getNodeTypeManager();

  protected abstract void registerNodeType() throws RepositoryException;

  protected NodeTypeTemplate createdNodeType(String nodeName) throws RepositoryException {
    NodeTypeTemplate type = getNodeTypeManager().createNodeTypeTemplate();

//    NodeTypeTemplate type = getNodeTypeManager().createNodeTypeTemplate();
    NodeTypeManager mgr = getSession().getWorkspace().getNodeTypeManager();
    NamespaceRegistry ns = getSession().getWorkspace().getNamespaceRegistry();
    if (!PEREGRINE_NAMESPACE_URI.equals(ns.getURI(PEREGRINE_NAMESPACE_URI))) {
      ns.registerNamespace(PEREGRINE_NAMESPACE_PREFIX, PEREGRINE_NAMESPACE_URI);
    }
    Optional<javax.jcr.nodetype.NodeType> oldType =
        mgr.hasNodeType(nodeName) ? Optional.of(mgr.getNodeType(nodeName)) : Optional.empty();

    type.setName(oldType.isPresent() ? oldType.get().getName() : nodeName);
    if (oldType.isPresent() && oldType.get().getDeclaredSupertypeNames().length > 0) {
      type.setDeclaredSuperTypeNames(oldType.get().getDeclaredSupertypeNames());
    }
//    else {
//      type.setDeclaredSuperTypeNames(perPageContentSupertypes);
//    }
    type.setAbstract(oldType.isPresent() && oldType.get().isAbstract());
    type.setMixin(oldType.isPresent() && oldType.get().isMixin());
    type.setOrderableChildNodes(oldType.isPresent() && oldType.get().hasOrderableChildNodes());
    if (oldType.isPresent() && oldType.get().getPrimaryItemName() != null) {
      type.setPrimaryItemName(oldType.get().getPrimaryItemName());
    } else {
      type.setPrimaryItemName(mgr.getNodeType(NT_UNSTRUCTURED).getPrimaryItemName());
    }
    type.setQueryable(oldType.isPresent() && oldType.get().isQueryable());

    return type;
//
//    // Default value
//    ValueFactory valueFactory = session.getValueFactory();
//    Value[] defaultValue = {valueFactory.createValue("sample text")};
//
//    // Add property to node type
//    type.getPropertyDefinitionTemplates().add(prop1);
//    type.getPropertyDefinitionTemplates().add(prop2);
//
//    if (manager.hasNodeType(PAGE_CONTENT_TYPE)) {
//      manager.unregisterNodeType(PAGE_CONTENT_TYPE);
//    }
//
//    /* Register node type */
//    manager.registerNodeType(type, false);
//    session.save();
  }

//  protected NodeTypeTemplate setPropertyDefinition(NodeTypeTemplate type)
//      throws RepositoryException {
//    PropertyDefinitionTemplate prop = getNodeTypeManager().createPropertyDefinitionTemplate();
//    type.getPropertyDefinitionTemplates().add(prop);
//    return type;
//  }
//
//  protected NodeTypeTemplate setNodeDefinition(NodeTypeTemplate type) throws RepositoryException {
//    NodeDefinitionTemplate node = getNodeTypeManager().createNodeDefinitionTemplate();
//    type.getNodeDefinitionTemplates().add(node);
//    return type;
}
}
