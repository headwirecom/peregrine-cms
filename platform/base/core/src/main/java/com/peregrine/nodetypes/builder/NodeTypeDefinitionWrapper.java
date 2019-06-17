package com.peregrine.nodetypes.builder;

@SuppressWarnings("unchecked")
public class NodeTypeDefinitionWrapper extends JcrItem {

  public static class Builder<C extends NodeTypeDefinitionWrapper, B extends Builder<C, B>> extends JcrItem.Builder<C, B> {
    public Builder() {
      this((C) new NodeTypeDefinitionWrapper());
    }

    protected Builder(C obj) {
      super(obj);
    }
  }
}