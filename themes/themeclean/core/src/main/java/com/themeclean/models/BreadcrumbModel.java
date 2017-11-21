package com.themeclean.models;


import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.Container;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

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
        "level": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Number Of Levels",
          "x-form-type": "number"
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
	
	private static final Logger LOG = LoggerFactory.getLogger(BreadcrumbModel.class);

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Number Of Levels","x-form-type":"number"} */
	@Inject
	private String level;


//GEN]
    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Number Of Levels","x-form-type":"number"} */
	public String getLevel() {
		return level;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
