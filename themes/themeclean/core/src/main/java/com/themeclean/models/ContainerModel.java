package com.themeclean.models;

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Container": {
      "type": "object",
      "x-type": "component",
      "properties": {}
    }
  },
  "name": "Container",
  "componentPath": "themeclean/components/container",
  "package": "com.themeclean.models",
  "modelName": "Container",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/container",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class ContainerModel extends Container {

    public ContainerModel(Resource r) { super(r); }

    //GEN[:INJECT
    
//GEN]

    //GEN[:GETTERS
    
//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
