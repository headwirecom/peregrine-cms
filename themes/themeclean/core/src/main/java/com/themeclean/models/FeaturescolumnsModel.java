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
    "Featurescolumns": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "showtitle": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Show Title",
          "x-form-type": "materialswitch"
        },
        "showtext": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Show Text",
          "x-form-type": "materialswitch"
        },
        "showbutton": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Show Button",
          "x-form-type": "materialswitch"
        },
        "cards": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Cards",
          "x-form-type": "collection",
          "properties": {
            "title": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Card Title",
              "x-form-visible": "model.showtitle == 'true'",
              "x-form-type": "text"
            },
            "text": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Card Text",
              "x-form-visible": "model.showtext == 'true'",
              "x-form-type": "texteditor"
            },
            "image": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Card Image",
              "x-form-type": "pathbrowser"
            },
            "buttontext": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Button Text",
              "x-form-visible": "model.showbutton == 'true'",
              "x-form-type": "text"
            },
            "buttonlink": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Button Link",
              "x-form-visible": "model.showbutton == 'true'",
              "x-form-type": "pathbrowser"
            },
            "buttoncolor": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Button Color",
              "x-form-visible": "model.showbutton == 'true'",
              "x-form-type": "color"
            }
          }
        },
        "bgref": {
          "x-form-type": "reference",
          "type": "object",
          "x-type": "component",
          "properties": {
            "backgroundtype": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Type",
              "x-form-type": "materialradio",
              "x-default": "color",
              "properties": {
                "image": {
                  "x-form-name": "Image",
                  "x-form-value": "image"
                },
                "color": {
                  "x-form-name": "Color",
                  "x-form-value": "color"
                },
                "gradient": {
                  "x-form-name": "Gradient",
                  "x-form-value": "gradient"
                },
                "video": {
                  "x-form-name": "Video",
                  "x-form-value": "video"
                }
              }
            },
            "bgvideo": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Video",
              "x-form-type": "pathbrowser",
              "x-form-visible": "model.backgroundtype == 'video'"
            },
            "bgimage": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Image",
              "x-form-type": "pathbrowser",
              "x-form-visible": "model.backgroundtype == 'image'"
            },
            "overlay": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Overlay",
              "x-form-type": "materialswitch",
              "x-form-visible": "model.backgroundtype == 'image'"
            },
            "overlaycolor": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Overlay Color",
              "x-form-type": "color",
              "x-form-visible": "model.overlay == 'true' and model.backgroundtype == 'image'",
              "x-default": "#ffffff"
            },
            "overlayopacity": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Overlay opacity",
              "x-form-type": "range",
              "x-form-min": 0,
              "x-form-max": 100,
              "x-form-visible": "model.overlay == 'true' and model.backgroundtype == 'image'",
              "x-default": "50"
            },
            "bgcolor": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Color",
              "x-form-type": "color",
              "x-form-visible": "model.backgroundtype == 'color' or model.backgroundtype == 'gradient'",
              "x-default": "#ffffff"
            },
            "color2": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Color 2",
              "x-form-type": "color",
              "x-form-visible": "model.backgroundtype == 'gradient'",
              "x-default": "#c0c0c0"
            },
            "fullwidth": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Full Width",
              "x-form-type": "materialswitch"
            },
            "fullheight": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Full Height",
              "x-form-type": "materialswitch"
            },
            "toppadding": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Top Padding",
              "x-form-type": "range",
              "x-form-min": 0,
              "x-form-max": 120,
              "x-form-visible": "model.fullheight != 'true'"
            },
            "bottompadding": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Bottom Padding",
              "x-form-type": "range",
              "x-form-min": 0,
              "x-form-max": 120,
              "x-form-visible": "model.fullheight != 'true'"
            }
          }
        }
      }
    }
  },
  "name": "Featurescolumns",
  "componentPath": "themeclean/components/featurescolumns",
  "package": "com.themeclean.models",
  "modelName": "Featurescolumns",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/featurescolumns",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class FeaturescolumnsModel extends AbstractComponent {

    public FeaturescolumnsModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Show Title","x-form-type":"materialswitch"} */
	@Inject
	private String showtitle;

	/* {"type":"string","x-source":"inject","x-form-label":"Show Text","x-form-type":"materialswitch"} */
	@Inject
	private String showtext;

	/* {"type":"string","x-source":"inject","x-form-label":"Show Button","x-form-type":"materialswitch"} */
	@Inject
	private String showbutton;

	/* {"type":"string","x-source":"inject","x-form-label":"Cards","x-form-type":"collection","properties":{"title":{"type":"string","x-source":"inject","x-form-label":"Card Title","x-form-visible":"model.showtitle == 'true'","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Card Text","x-form-visible":"model.showtext == 'true'","x-form-type":"texteditor"},"image":{"type":"string","x-source":"inject","x-form-label":"Card Image","x-form-type":"pathbrowser"},"buttontext":{"type":"string","x-source":"inject","x-form-label":"Button Text","x-form-visible":"model.showbutton == 'true'","x-form-type":"text"},"buttonlink":{"type":"string","x-source":"inject","x-form-label":"Button Link","x-form-visible":"model.showbutton == 'true'","x-form-type":"pathbrowser"},"buttoncolor":{"type":"string","x-source":"inject","x-form-label":"Button Color","x-form-visible":"model.showbutton == 'true'","x-form-type":"color"}}} */
	@Inject
	private List<IComponent> cards;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-default":"color","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"},"video":{"x-form-name":"Video","x-form-value":"video"}}} */
	@Inject
	@Default(values ="color")
	private String backgroundtype;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Video","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'video'"} */
	@Inject
	private String bgvideo;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Image","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'image'"} */
	@Inject
	private String bgimage;

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay","x-form-type":"materialswitch","x-form-visible":"model.backgroundtype == 'image'"} */
	@Inject
	private String overlay;

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay Color","x-form-type":"color","x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image'","x-default":"#ffffff"} */
	@Inject
	@Default(values ="#ffffff")
	private String overlaycolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay opacity","x-form-type":"range","x-form-min":0,"x-form-max":100,"x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image'","x-default":"50"} */
	@Inject
	@Default(values ="50")
	private String overlayopacity;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Color","x-form-type":"color","x-form-visible":"model.backgroundtype == 'color' or model.backgroundtype == 'gradient'","x-default":"#ffffff"} */
	@Inject
	@Default(values ="#ffffff")
	private String bgcolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Color 2","x-form-type":"color","x-form-visible":"model.backgroundtype == 'gradient'","x-default":"#c0c0c0"} */
	@Inject
	@Default(values ="#c0c0c0")
	private String color2;

	/* {"type":"string","x-source":"inject","x-form-label":"Full Width","x-form-type":"materialswitch"} */
	@Inject
	private String fullwidth;

	/* {"type":"string","x-source":"inject","x-form-label":"Full Height","x-form-type":"materialswitch"} */
	@Inject
	private String fullheight;

	/* {"type":"string","x-source":"inject","x-form-label":"Top Padding","x-form-type":"range","x-form-min":0,"x-form-max":120,"x-form-visible":"model.fullheight != 'true'"} */
	@Inject
	private String toppadding;

	/* {"type":"string","x-source":"inject","x-form-label":"Bottom Padding","x-form-type":"range","x-form-min":0,"x-form-max":120,"x-form-visible":"model.fullheight != 'true'"} */
	@Inject
	private String bottompadding;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Show Title","x-form-type":"materialswitch"} */
	public String getShowtitle() {
		return showtitle;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Show Text","x-form-type":"materialswitch"} */
	public String getShowtext() {
		return showtext;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Show Button","x-form-type":"materialswitch"} */
	public String getShowbutton() {
		return showbutton;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Cards","x-form-type":"collection","properties":{"title":{"type":"string","x-source":"inject","x-form-label":"Card Title","x-form-visible":"model.showtitle == 'true'","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Card Text","x-form-visible":"model.showtext == 'true'","x-form-type":"texteditor"},"image":{"type":"string","x-source":"inject","x-form-label":"Card Image","x-form-type":"pathbrowser"},"buttontext":{"type":"string","x-source":"inject","x-form-label":"Button Text","x-form-visible":"model.showbutton == 'true'","x-form-type":"text"},"buttonlink":{"type":"string","x-source":"inject","x-form-label":"Button Link","x-form-visible":"model.showbutton == 'true'","x-form-type":"pathbrowser"},"buttoncolor":{"type":"string","x-source":"inject","x-form-label":"Button Color","x-form-visible":"model.showbutton == 'true'","x-form-type":"color"}}} */
	public List<IComponent> getCards() {
		return cards;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-default":"color","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"},"video":{"x-form-name":"Video","x-form-value":"video"}}} */
	public String getBackgroundtype() {
		return backgroundtype;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Video","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'video'"} */
	public String getBgvideo() {
		return bgvideo;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Image","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'image'"} */
	public String getBgimage() {
		return bgimage;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay","x-form-type":"materialswitch","x-form-visible":"model.backgroundtype == 'image'"} */
	public String getOverlay() {
		return overlay;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay Color","x-form-type":"color","x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image'","x-default":"#ffffff"} */
	public String getOverlaycolor() {
		return overlaycolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay opacity","x-form-type":"range","x-form-min":0,"x-form-max":100,"x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image'","x-default":"50"} */
	public String getOverlayopacity() {
		return overlayopacity;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Color","x-form-type":"color","x-form-visible":"model.backgroundtype == 'color' or model.backgroundtype == 'gradient'","x-default":"#ffffff"} */
	public String getBgcolor() {
		return bgcolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Color 2","x-form-type":"color","x-form-visible":"model.backgroundtype == 'gradient'","x-default":"#c0c0c0"} */
	public String getColor2() {
		return color2;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Full Width","x-form-type":"materialswitch"} */
	public String getFullwidth() {
		return fullwidth;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Full Height","x-form-type":"materialswitch"} */
	public String getFullheight() {
		return fullheight;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Top Padding","x-form-type":"range","x-form-min":0,"x-form-max":120,"x-form-visible":"model.fullheight != 'true'"} */
	public String getToppadding() {
		return toppadding;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Bottom Padding","x-form-type":"range","x-form-min":0,"x-form-max":120,"x-form-visible":"model.fullheight != 'true'"} */
	public String getBottompadding() {
		return bottompadding;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
