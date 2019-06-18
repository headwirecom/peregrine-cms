package com.peregrine.nodetypes.builder;

import com.peregrine.nodetypes.builder.NodeTypeTemplateFactory.NodeTypeTemplateBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.version.OnParentVersionAction;

public class NodeDefinitionTemplateFactory {

  protected static final class NodeDefinitionTemplateBuilder extends
      NodeDefinitionBuilder<NodeDefinitionTemplate, NodeDefinitionTemplateBuilder> {

    private final NodeTypeTemplateBuilder ntd;
    private final NodeDefinitionTemplate template;
    private final List<String> requiredPrimaryTypes = new ArrayList<String>();

    NodeDefinitionTemplateBuilder(NodeTypeManager nodeTypeManager)
        throws UnsupportedRepositoryOperationException, RepositoryException {
      super();
      this.ntd = ntd; // TODO:
      this.template = nodeTypeManager.createNodeDefinitionTemplate();
    }

    @Override
    protected NodeDefinitionTemplateBuilder self() {
      return this;
    }

    @Override
    public NodeDefinitionTemplate build() throws ConstraintViolationException {
      template.setAutoCreated(super.autocreate);
      template.setMandatory(super.isMandatory);
      template.setOnParentVersion(super.onParent);
      template.setProtected(super.isProtected);
      template.setRequiredPrimaryTypeNames(
          requiredPrimaryTypes.toArray(new String[requiredPrimaryTypes.size()]));
      template.setSameNameSiblings(super.allowSns);


      return template;
    }

    @Override
    public NodeDefinitionTemplateBuilder setName(String name) throws RepositoryException {
      super.setName(name);
      template.setName(name);
      return this;
    }

    @Override
    public NodeDefinitionTemplateBuilder addRequiredPrimaryType(String name) {
      requiredPrimaryTypes.add(name);
      return this;
    }

    @Override
    public NodeDefinitionTemplateBuilder setDefaultPrimaryType(String name)
        throws ConstraintViolationException {
      template.setDefaultPrimaryTypeName(name);
      return this;
    }

    @Override
    public void setDeclaringNodeType(String name) throws RepositoryException {
      // empty
    }
  }

  /**
   * Builder for child node definitions of type <code>T</code>.
   *
   * @param <T> type of the node type definition
   * @param <B> builder of the node type definition
   */
  public static abstract class NodeDefinitionBuilder<T extends NodeDefinition, B extends NodeDefinitionBuilder<T, B>> extends
      ItemDefinitionBuilder<T, B> {

    /**
     * See {@link #setAllowsSameNameSiblings(boolean)}
     */
    protected boolean allowSns;

    /**
     * @param name add a required primary type to the list of names of the required primary types of
     * the node definition being built.
     */
    public abstract B addRequiredPrimaryType(String name) throws RepositoryException;

    /**
     * @param name the name of the default primary type of the node definition being built.
     * @throws RepositoryException if an error occurs
     */
    public abstract B setDefaultPrimaryType(String name) throws RepositoryException;

    /**
     * @param allowSns true if building a node definition with same name siblings, false otherwise.
     * @throws RepositoryException if an error occurs
     */
    public B setAllowsSameNameSiblings(boolean allowSns) throws RepositoryException {
      this.allowSns = allowSns;
      return this.self();
    }
  }

  /**
   * Builder for item definitions of type <code>T</code>.
   *
   * @param <T> type of the node type definition
   * @param <B> builder of the node type definition
   */
  public static abstract class ItemDefinitionBuilder<T extends ItemDefinition, B extends ItemDefinitionBuilder<T, B>> {

    /**
     * See {@link #setName(String)}
     */
    protected String name;

    /**
     * See {@link #setAutoCreated(boolean)}
     */
    protected boolean autocreate;

    /**
     * See {@link #setMandatory(boolean)}
     */
    protected boolean isMandatory;

    /**
     * See {@link #setOnParentVersion(int)}
     */
    protected int onParent;

    /**
     * See {@link #setProtected(boolean)}
     */
    protected boolean isProtected;

    public ItemDefinitionBuilder() {
    }

    protected abstract B self();

    /**
     * Build this item definition an add it to its parent node type definition.
     *
     * @throws RepositoryException if an error occurs
     */
    public abstract T build() throws RepositoryException;

    /**
     * @param name the name of the declaring node type.
     * @throws RepositoryException if an error occurs
     * @see ItemDefinition#getDeclaringNodeType()
     */
    public abstract void setDeclaringNodeType(String name) throws RepositoryException;

    /**
     * @param name the name of the child item definition being build.
     * @see ItemDefinition#getName()
     */
    public B setName(String name) throws RepositoryException {
      this.name = name;
      return this.self();
    }

    /**
     * @param autocreate <code>true</code> if building a 'autocreate' child item definition, false
     * otherwise.
     * @throws RepositoryException if an error occurs
     * @see ItemDefinition#isAutoCreated()
     */
    public B setAutoCreated(boolean autocreate) throws RepositoryException {
      this.autocreate = autocreate;
      return this.self();
    }

    /**
     * @param isMandatory <code>true</code> if building a 'mandatory' child item definition, false
     * otherwise.
     * @throws RepositoryException if an error occurs
     */
    public B setMandatory(boolean isMandatory) throws RepositoryException {
      this.isMandatory = isMandatory;
      return this.self();
    }

    /**
     * @param onParent the 'onParentVersion' attribute of the child item definition being built.
     * @throws RepositoryException if an error occurs
     * @see ItemDefinition#getOnParentVersion()
     */
    public B setOnParentVersion(int onParent) throws RepositoryException {
      this.onParent = onParent;
      return this.self();
    }

    /**
     * @param onParent the 'onParentVersion' attribute of the child item definition being built.
     * @throws RepositoryException if an error occurs
     * @see ItemDefinition#getOnParentVersion()
     */
    public B setOnParentVersion(String onParent) throws RepositoryException {
      this.onParent = OnParentVersionAction.valueFromName(onParent);
      return this.self();
    }

    /**
     * @param isProtected <code>true</code> if building a 'protected' child item definition, false
     * otherwise.
     * @throws RepositoryException if an error occurs
     * @see ItemDefinition#isProtected()
     */
    public B setProtected(boolean isProtected) throws RepositoryException {
      this.isProtected = isProtected;
      return this.self();
    }
  }
}
