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
    "Breadcrumb": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "rootpath": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Root Path",
          "x-form-type": "pathbrowser",
          "x-form-browserRoot": "/content/sites"
        }
      }
    }
  },
  "name": "Breadcrumb",
  "componentPath": "themeclean/components/breadcrumb",
  "package": "com.themeclean.models",
  "modelName": "Breadcrumb",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/breadcrumb",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class BreadcrumbModel extends AbstractComponent {

    public BreadcrumbModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Root Path","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"} */
	@Inject
	private String rootpath;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Root Path","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"} */
	public String getRootpath() {
		return rootpath;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
