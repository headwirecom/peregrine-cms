package com.uswds.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.Container;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Sectiongraphiclist": {
      "type": "object",
      "x-type": "container",
      "properties": {}
    }
  },
  "name": "Sectiongraphiclist",
  "componentPath": "uswds/components/sectiongraphiclist",
  "package": "com.uswds.models",
  "modelName": "Sectiongraphiclist",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "uswds/components/sectiongraphiclist",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class SectiongraphiclistModel extends Container {

    public SectiongraphiclistModel(Resource r) { super(r); }

    //GEN[:INJECT
    
//GEN]

    //GEN[:GETTERS
    
//GEN]

}
