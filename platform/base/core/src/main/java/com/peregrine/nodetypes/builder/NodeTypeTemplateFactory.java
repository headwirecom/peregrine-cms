package com.peregrine.nodetypes.builder;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;

public class NodeTypeTemplateFactory {

  public static NodeTypeDefinitionBuilder newNodeTypeDefinitionBuilder(Session session)
      throws UnsupportedRepositoryOperationException, RepositoryException {
    return new NodeTypeTemplateBuilder(session.getWorkspace().getNodeTypeManager());
  }

  protected static final class NodeTypeTemplateBuilder extends
      NodeTypeDefinitionBuilder<NodeTypeTemplate, NodeTypeTemplateBuilder> {

    private final NodeTypeTemplate template;
    private final List<String> supertypes = new ArrayList<String>();

    NodeTypeTemplateBuilder(NodeTypeManager nodeTypeManager)
        throws UnsupportedRepositoryOperationException, RepositoryException {
      super();
      this.template = nodeTypeManager.createNodeTypeTemplate();
    }

    @Override
    protected NodeTypeTemplateBuilder self() {
      return this;
    }

    @Override
    public NodeTypeTemplate build() throws ConstraintViolationException {
      template.setMixin(super.isMixin);
      template.setOrderableChildNodes(super.isOrderable);
      template.setAbstract(super.isAbstract);
      template.setQueryable(super.queryable);
      template.setDeclaredSuperTypeNames(supertypes.toArray(new String[supertypes.size()]));
      return template;
    }

    @Override
    public NodeTypeTemplateBuilder setName(String name) throws RepositoryException {
      super.setName(name);
      template.setName(name);
      return this;
    }

    @Override
    public NodeTypeTemplateBuilder addSupertype(String name) {
      supertypes.add(name);
      return this;
    }

    @Override
    public NodeTypeTemplateBuilder setPrimaryItemName(String name)
        throws ConstraintViolationException {
      template.setPrimaryItemName(name);
      return this;
    }
  }

  /**
   * Builder for a node type definition of type <code>T</code>.
   *
   * @param <T> type of the node type definition
   * @param <B> builder of the node type definition
   */
  public static abstract class NodeTypeDefinitionBuilder<T extends NodeTypeDefinition, B extends NodeTypeDefinitionBuilder<T, B>> {

    /**
     * See {@link #setName(String)}
     */
    protected String name;

    /**
     * See {@link #setAbstract(boolean)}
     */
    protected boolean isAbstract;

    /**
     * See {@link #setMixin(boolean)}
     */
    protected boolean isMixin;

    /**
     * See {@link #setOrderableChildNodes(boolean)}
     */
    protected boolean isOrderable;

    /**
     * See {@link #setQueryable(boolean)}
     */
    protected boolean queryable;

    public NodeTypeDefinitionBuilder() {
    }

    protected abstract B self();

    /**
     * Build this node type definition.
     *
     * @return type of the node type definition
     * @throws RepositoryException if an error occurs
     */
    public abstract T build() throws RepositoryException;

    /**
     * Set the name of the node type definition being built.
     *
     * @param name the name of the node type
     * @throws RepositoryException if the name is not valid
     * @see NodeTypeDefinition#getName()
     */
    public B setName(final String name) throws RepositoryException {
      this.name = name;
      return this.self();
    }

    /**
     * Add the given name to the set of supertypes of the node type definition being built.
     *
     * @param name the name of the the supertype
     * @throws RepositoryException if the name is not valid
     * @see NodeTypeDefinition#getDeclaredSupertypeNames()
     */
    public abstract B addSupertype(String name) throws RepositoryException;

    /**
     * @param isAbstract <code>true</code> if building a node type that is abstract.
     * @throws RepositoryException if an error occurs
     * @see NodeTypeDefinition#isAbstract()
     */
    public B setAbstract(final boolean isAbstract) throws RepositoryException {
      this.isAbstract = isAbstract;
      return this.self();
    }

    /**
     * @param isMixin <code>true</code> if building a mixin node type definition; <code>false</code>
     * otherwise.
     * @throws RepositoryException if an error occurs
     * @see NodeTypeDefinition#isMixin()
     */
    public B setMixin(final boolean isMixin) throws RepositoryException {
      this.isMixin = isMixin;
      return this.self();
    }

    /**
     * @param isOrderable <code>true</code> if building a node type having orderable child nodes;
     * <code>false</code> otherwise.
     * @throws RepositoryException if an error occurs
     * @see NodeTypeDefinition#hasOrderableChildNodes()
     */
    public B setOrderableChildNodes(final boolean isOrderable) throws RepositoryException {
      this.isOrderable = isOrderable;
      return this.self();
    }

    /**
     * @param name the name of the primary item.
     * @throws RepositoryException if an error occurs
     * @see NodeTypeDefinition#getPrimaryItemName()
     */
    public abstract B setPrimaryItemName(String name) throws RepositoryException;

    /**
     * @param queryable <code>true</code> if building a node type that is queryable
     * @throws RepositoryException if an error occurs
     * @see NodeTypeDefinition#isQueryable()
     */
    public B setQueryable(final boolean queryable) throws RepositoryException {
      this.queryable = queryable;
      return this.self();
    }
  }
}