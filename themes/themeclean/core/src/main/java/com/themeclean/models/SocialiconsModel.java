package com.themeclean.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import java.util.List;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Socialicons": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "iconcustomcolor": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Custom Icons Color",
          "x-form-type": "materialswitch"
        },
        "iconcolor": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Icon Color",
          "x-default": "#000000",
          "x-form-visible": "model.iconcustomcolor == 'true'",
          "x-form-type": "color"
        },
        "iconsize": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Icon Size",
          "x-form-type": "materialrange",
          "x-default": "25",
          "x-form-min": 0,
          "x-form-max": 150
        },
        "icons": {
          "type": "object",
          "x-form-type": "collection",
          "x-form-label": "Icons",
          "x-source": "inject",
          "properties": {
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
            },
            "url": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Icon Url",
              "x-form-type": "pathbrowser",
              "x-form-browserRoot": "/content/themeclean/pages"
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
    	/* {"type":"string","x-source":"inject","x-form-label":"Custom Icons Color","x-form-type":"materialswitch"} */
	@Inject
	private String iconcustomcolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Color","x-default":"#000000","x-form-visible":"model.iconcustomcolor == 'true'","x-form-type":"color"} */
	@Inject
	@Default(values ="#000000")
	private String iconcolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Size","x-form-type":"materialrange","x-default":"25","x-form-min":0,"x-form-max":150} */
	@Inject
	@Default(values ="25")
	private String iconsize;

	/* {"type":"object","x-form-type":"collection","x-form-label":"Icons","x-source":"inject","properties":{"icon":{"type":"string","x-source":"inject","x-form-label":"Icon Chooser","x-form-type":"iconbrowser","x-form-hint":"Select an icon.","x-form-required":true,"x-form-validator":"required","x-form-families":["material","font awesome"]},"url":{"type":"string","x-source":"inject","x-form-label":"Icon Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/pages"}}} */
	@Inject
	private List<IComponent> icons;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Custom Icons Color","x-form-type":"materialswitch"} */
	public String getIconcustomcolor() {
		return iconcustomcolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Color","x-default":"#000000","x-form-visible":"model.iconcustomcolor == 'true'","x-form-type":"color"} */
	public String getIconcolor() {
		return iconcolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Size","x-form-type":"materialrange","x-default":"25","x-form-min":0,"x-form-max":150} */
	public String getIconsize() {
		return iconsize;
	}

	/* {"type":"object","x-form-type":"collection","x-form-label":"Icons","x-source":"inject","properties":{"icon":{"type":"string","x-source":"inject","x-form-label":"Icon Chooser","x-form-type":"iconbrowser","x-form-hint":"Select an icon.","x-form-required":true,"x-form-validator":"required","x-form-families":["material","font awesome"]},"url":{"type":"string","x-source":"inject","x-form-label":"Icon Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/pages"}}} */
	public List<IComponent> getIcons() {
		return icons;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
