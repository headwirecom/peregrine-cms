package com.peregrine.nodetypes.builder;

public class Parent {

  private KidA kidA;
  private KidB kidB;

  public Parent(Builder builder) {
    this.kidA = builder.kidA;
    this.kidB = builder.kidB;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {

    private KidA kidA;
    private KidB kidB;

    private Builder() {
    }

    public Builder withKidA(KidA kidA) {
      this.kidA = kidA;
      return this;
    }

    public Builder withKidB(KidB kidB) {
      this.kidB = kidB;
      return this;
    }

    // to add manually
    private KidA.Builder builderKidA = KidA.newBuilder().withParentBuilder(this);
    private KidB.Builder builderKidB = KidB.newBuilder().withParentBuilder(this);

    public KidA.Builder addKidA() {
      return this.builderKidA;
    }

    public KidB.Builder addKidB() {
      return this.builderKidB;
    }
    //---------

    public Parent build() {
      return new Parent(this);
    }
  }
}