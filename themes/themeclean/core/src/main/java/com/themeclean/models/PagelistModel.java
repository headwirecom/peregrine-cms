package com.themeclean.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.Container;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Pagelist": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "rootpage": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "pathbrowser",
          "x-form-label": "Root Page",
          "x-form-browserRoot": "/content/sites"
        },
        "includeroot": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Include Root",
          "x-form-type": "materialcheckbox",
          "x-form-default": false
        },
        "levels": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "number",
          "x-form-label": "Levels",
          "x-form-default": 1
        }
      }
    }
  },
  "name": "Pagelist",
  "componentPath": "themeclean/components/pagelist",
  "package": "com.themeclean.models",
  "modelName": "Pagelist",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/pagelist",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PagelistModel extends AbstractComponent {

    public PagelistModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-type":"pathbrowser","x-form-label":"Root Page","x-form-browserRoot":"/content/sites"} */
	@Inject
	private String rootpage;

	/* {"type":"string","x-source":"inject","x-form-label":"Include Root","x-form-type":"materialcheckbox","x-form-default":false} */
	@Inject
	private String includeroot;

	/* {"type":"string","x-source":"inject","x-form-type":"number","x-form-label":"Levels","x-form-default":1} */
	@Inject
	private String levels;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-type":"pathbrowser","x-form-label":"Root Page","x-form-browserRoot":"/content/sites"} */
	public String getRootpage() {
		return rootpage;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Include Root","x-form-type":"materialcheckbox","x-form-default":false} */
	public String getIncluderoot() {
		return includeroot;
	}

	/* {"type":"string","x-source":"inject","x-form-type":"number","x-form-label":"Levels","x-form-default":1} */
	public String getLevels() {
		return levels;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
