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
    "Articleheader": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "showtitle": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Show Title",
          "x-form-type": "materialswitch"
        },
        "title": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Title",
          "x-form-visible": "model.showtitle == 'true'",
          "x-form-type": "text"
        },
        "showsubtitle": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Show Subtitle",
          "x-form-type": "materialswitch"
        },
        "subtitle": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Subtitle",
          "x-form-visible": "model.showsubtitle == 'true'",
          "x-form-type": "text"
        },
        "textalign": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Text align",
          "x-form-type": "materialradio",
          "properties": {
            "left": {
              "x-form-name": "Left",
              "x-form-value": "text-left"
            },
            "center": {
              "x-form-name": "Center",
              "x-form-value": "text-center"
            },
            "right": {
              "x-form-name": "Right",
              "x-form-value": "text-right"
            }
          }
        },
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
            }
          }
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
  },
  "name": "Articleheader",
  "componentPath": "themeclean/components/articleheader",
  "package": "com.themeclean.models",
  "modelName": "Articleheader",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/articleheader",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class ArticleheaderModel extends AbstractComponent {

    public ArticleheaderModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Show Title","x-form-type":"materialswitch"} */
	@Inject
	private String showtitle;

	/* {"type":"string","x-source":"inject","x-form-label":"Title","x-form-visible":"model.showtitle == 'true'","x-form-type":"text"} */
	@Inject
	private String title;

	/* {"type":"string","x-source":"inject","x-form-label":"Show Subtitle","x-form-type":"materialswitch"} */
	@Inject
	private String showsubtitle;

	/* {"type":"string","x-source":"inject","x-form-label":"Subtitle","x-form-visible":"model.showsubtitle == 'true'","x-form-type":"text"} */
	@Inject
	private String subtitle;

	/* {"type":"string","x-source":"inject","x-form-label":"Text align","x-form-type":"materialradio","properties":{"left":{"x-form-name":"Left","x-form-value":"text-left"},"center":{"x-form-name":"Center","x-form-value":"text-center"},"right":{"x-form-name":"Right","x-form-value":"text-right"}}} */
	@Inject
	private String textalign;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-default":"color","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"}}} */
	@Inject
	@Default(values ="color")
	private String backgroundtype;

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

	/* {"type":"string","x-source":"inject","x-form-label":"Title","x-form-visible":"model.showtitle == 'true'","x-form-type":"text"} */
	public String getTitle() {
		return title;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Show Subtitle","x-form-type":"materialswitch"} */
	public String getShowsubtitle() {
		return showsubtitle;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Subtitle","x-form-visible":"model.showsubtitle == 'true'","x-form-type":"text"} */
	public String getSubtitle() {
		return subtitle;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Text align","x-form-type":"materialradio","properties":{"left":{"x-form-name":"Left","x-form-value":"text-left"},"center":{"x-form-name":"Center","x-form-value":"text-center"},"right":{"x-form-name":"Right","x-form-value":"text-right"}}} */
	public String getTextalign() {
		return textalign;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-default":"color","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"}}} */
	public String getBackgroundtype() {
		return backgroundtype;
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
