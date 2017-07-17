package com.blog.models;

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
    "Header": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "title": {
          "type": "string",
          "x-source": "inject"
        },
        "description": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  },
  "name": "Header",
  "componentPath": "blog/components/header",
  "package": "com.blog.models",
  "modelName": "Header",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "blog/components/header",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class HeaderModel extends AbstractComponent {

    public HeaderModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String title;

	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String description;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject"} */
	public String getTitle() {
		return title;
	}

	/* {"type":"string","x-source":"inject"} */
	public String getDescription() {
		return description;
	}


//GEN]

}
