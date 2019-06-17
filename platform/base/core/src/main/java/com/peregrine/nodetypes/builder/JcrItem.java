package com.peregrine.nodetypes.builder;

@SuppressWarnings("unchecked")
public abstract class JcrItem {
  protected String name;

  public static class Builder<C extends JcrItem, B extends Builder<C, B>> {
    C obj;

    protected Builder(C constructedObj) {
      this.obj = constructedObj;
    }

    public B withName(String name) {
      obj.name = name;
      return (B)this;
    }

    public C build() {
      return obj;
    }
  }
}