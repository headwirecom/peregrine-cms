package com.peregrine.admin.models;

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

import static com.peregrine.admin.util.AdminConstants.DROPDOWN_COMPONENT_PATH;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;


@Model(
    adaptables = Resource.class,
    resourceType = DROPDOWN_COMPONENT_PATH,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    adapters = IComponent.class
)
@Exporter(name = JACKSON,
    extensions = JSON)

public class DropdownModel extends Container {


  public DropdownModel(Resource r) { super(r); }

}

