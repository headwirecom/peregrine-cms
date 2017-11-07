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
    "Socialicons": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "icons": {
          "type": "object",
          "x-form-type": "collection",
          "x-form-label": "Icons",
          "x-source": "inject",
          "properties": {
            "url": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Icon Url",
              "x-form-type": "pathbrowser"
            },
            "icon": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Icon Chooser",
              "x-form-type": "iconbrowser",
              "x-form-hint": "Select an icon.",
              "x-form-required": true,
              "x-form-validator": "required",
              "x-form-families": [
                "material",
                "font awesome"
              ]
            }
          }
        }
      }
    }
  },
  "name": "Socialicons",
  "componentPath": "themeclean/components/socialicons",
  "package": "com.themeclean.models",
  "modelName": "Socialicons",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/socialicons",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class SocialiconsModel extends AbstractComponent {

    public SocialiconsModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"object","x-form-type":"collection","x-form-label":"Icons","x-source":"inject","properties":{"url":{"type":"string","x-source":"inject","x-form-label":"Icon Url","x-form-type":"pathbrowser"},"icon":{"type":"string","x-source":"inject","x-form-label":"Icon Chooser","x-form-type":"iconbrowser","x-form-hint":"Select an icon.","x-form-required":true,"x-form-validator":"required","x-form-families":["material","font awesome"]}}} */
	@Inject
	private List<IComponent> icons;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"object","x-form-type":"collection","x-form-label":"Icons","x-source":"inject","properties":{"url":{"type":"string","x-source":"inject","x-form-label":"Icon Url","x-form-type":"pathbrowser"},"icon":{"type":"string","x-source":"inject","x-form-label":"Icon Chooser","x-form-type":"iconbrowser","x-form-hint":"Select an icon.","x-form-required":true,"x-form-validator":"required","x-form-families":["material","font awesome"]}}} */
	public List<IComponent> getIcons() {
		return icons;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
