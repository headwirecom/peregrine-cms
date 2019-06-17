package com.peregrine.nodetypes.builder;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.version.OnParentVersionAction;

public class PropertyDefinitionBuilder {

  private String name;
  private boolean isAutoCreated;
  private boolean isMandatory;
  private OnParentVersionAction onParentVersionStatus;
  private boolean isProtected;
  private PropertyType requiredType;
  private String[] valueConstraints;
  private Value[] defaultValues;
  private boolean isMultiple;
  private String[] availableQueryOperators;
  private boolean isFullTextSearchable;
  private boolean isQueryOrderable;

  private PropertyDefinitionBuilder(Builder builder) {

  }

//  public static Builder newBuilder() {
//    return new Builder();
//  }
//
//  public static Builder newBuilder() {
//
//  }


  private static final class Builder {

    private String name;
    private boolean isAutoCreated;
    private boolean isMandatory;
    private OnParentVersionAction onParentVersionStatus;
    private boolean isProtected;
    private PropertyType requiredType;
    private String[] valueConstraints;
    private Value[] defaultValues;
    private boolean isMultiple;
    private String[] availableQueryOperators;
    private boolean isFullTextSearchable;
    private boolean isQueryOrderable;

    private Builder() {
    }


    public PropertyDefinitionBuilder build() {
      return new PropertyDefinitionBuilder(this);
    }
  }
}
