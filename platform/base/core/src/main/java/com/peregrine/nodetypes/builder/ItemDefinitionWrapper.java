package com.peregrine.nodetypes.builder;

@SuppressWarnings("unchecked")
public class ItemDefinitionWrapper extends JcrItem {

  public static class Builder<C extends ItemDefinitionWrapper, B extends Builder<C, B>> extends JcrItem.Builder<C, B> {
    public Builder() {
      this((C) new ItemDefinitionWrapper());
    }

    protected Builder(C obj) {
      super(obj);
    }
  }
}