package com.experiences.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Reference": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "ref": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  },
  "name": "Reference",
  "componentPath": "experiences/components/reference",
  "package": "com.experiences.models",
  "modelName": "Reference",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "experiences/components/reference",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class ReferenceModel extends AbstractComponent {

    public ReferenceModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String ref;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject"} */
	public String getRef() {
		return ref;
	}


//GEN]

}
