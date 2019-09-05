package com.experiences.models;

import com.peregrine.nodetypes.models.Container;
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
    "Experience": {
      "type": "object",
      "x-type": "container",
      "properties": {
        "experiences": {
          "type": "object",
          "x-source": "inject",
          "x-form-type": "collection",
          "properties": {
            "key": {
              "type": "string"
            }
          }
        }
      }
    }
  },
  "name": "Experience",
  "componentPath": "experiences/components/experience",
  "package": "com.experiences.models",
  "modelName": "Experience",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "experiences/components/experience",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class ExperienceModel extends Container {

    public ExperienceModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"object","x-source":"inject","x-form-type":"collection","properties":{"key":{"type":"string"}}} */
	@Inject
	private String[] experiences;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"object","x-source":"inject","x-form-type":"collection","properties":{"key":{"type":"string"}}} */
	public String[] getExperiences() {
		return experiences;
	}


//GEN]

}
