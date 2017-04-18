package com.headwire.cms.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface IComponent {

    @JsonIgnore
    Resource getResource();

    List<IComponent> getChildren();

}