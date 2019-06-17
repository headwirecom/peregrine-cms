package com.peregrine.nodetypes.builder;

@SuppressWarnings("unchecked")
public class Class3 extends NodeTypeDefinitionWrapper {
  protected int f3;

  public static class Builder<C extends Class3, B extends Builder<C, B>> extends NodeTypeDefinitionWrapper.Builder<C, B> {
    public Builder() {
      setObj((C) new Class3());
    }

    public Builder(C obj) {
      this();
      copy(obj);
    }

    @Override
    protected void copy(C obj) {
      super.copy(obj);
      this.f3(obj.f3);
    }

    public B f3(int f3) {
      obj.f3 = f3;
      return (B)this;
    }
  }
}