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
    "Menu": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "logo": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Logo",
          "x-form-type": "pathbrowser",
          "x-form-browserRoot": "/content/assets"
        },
        "logourl": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Logo Url",
          "x-form-type": "pathbrowser",
          "x-form-browserRoot": "/content/sites"
        },
        "logosize": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Logo Size",
          "x-form-type": "range",
          "x-form-min": 1,
          "x-form-max": 10
        },
        "collapsed": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Collapse Menu",
          "x-form-type": "materialswitch"
        },
        "sticky": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Sticky",
          "x-form-type": "materialswitch"
        },
        "linksref": {
          "x-form-type": "reference",
          "type": "object",
          "x-type": "component",
          "properties": {
            "links": {
              "type": "object",
              "x-source": "inject",
              "x-form-type": "collection",
              "x-form-label": "Links",
              "properties": {
                "text": {
                  "type": "string",
                  "x-source": "inject",
                  "x-form-label": "Link Text",
                  "x-form-type": "text"
                },
                "link": {
                  "type": "string",
                  "x-source": "inject",
                  "x-form-label": "Link Url",
                  "x-form-type": "pathbrowser",
                  "x-form-browserRoot": "/content/sites"
                }
              }
            }
          }
        },
        "buttonsref": {
          "x-form-type": "reference",
          "type": "object",
          "x-type": "component",
          "properties": {
            "buttons": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Buttons",
              "x-form-type": "collection",
              "properties": {
                "buttontext": {
                  "type": "string",
                  "x-source": "inject",
                  "x-form-label": "Button Text",
                  "x-form-type": "text"
                },
                "buttonlink": {
                  "type": "string",
                  "x-source": "inject",
                  "x-form-label": "Button Link",
                  "x-form-type": "pathbrowser",
                  "x-form-browserRoot": "/content/sites"
                },
                "buttoncolor": {
                  "type": "string",
                  "x-source": "inject",
                  "x-form-label": "Button Color",
                  "x-form-type": "color"
                }
              }
            }
          }
        },
        "bgref": {
          "x-form-type": "reference",
          "type": "object",
          "x-type": "component",
          "properties": {
            "colorscheme": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Block Color Scheme",
              "x-form-type": "materialradio",
              "x-default": "light",
              "properties": {
                "light": {
                  "x-form-name": "Light",
                  "x-form-value": "light"
                },
                "dark": {
                  "x-form-name": "Dark",
                  "x-form-value": "dark"
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
              "x-form-visible": "model.backgroundtype == 'video'",
              "x-default": "https://www.youtube.com/embed/Ju86mknumYM",
              "x-form-browserRoot": "/content/assets"
            },
            "bgimage": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Image",
              "x-form-type": "pathbrowser",
              "x-form-visible": "model.backgroundtype == 'image'",
              "x-form-browserRoot": "/content/assets"
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
              "x-form-max": 150,
              "x-form-visible": "model.fullheight != 'true'"
            }
          }
        }
      }
    }
  },
  "name": "Menu",
  "componentPath": "themeclean/components/menu",
  "package": "com.themeclean.models",
  "modelName": "Menu",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/menu",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class MenuModel extends AbstractComponent {

    public MenuModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Logo","x-form-type":"pathbrowser","x-form-browserRoot":"/content/assets"} */
	@Inject
	private String logo;

	/* {"type":"string","x-source":"inject","x-form-label":"Logo Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"} */
	@Inject
	private String logourl;

	/* {"type":"string","x-source":"inject","x-form-label":"Logo Size","x-form-type":"range","x-form-min":1,"x-form-max":10} */
	@Inject
	private String logosize;

	/* {"type":"string","x-source":"inject","x-form-label":"Collapse Menu","x-form-type":"materialswitch"} */
	@Inject
	private String collapsed;

	/* {"type":"string","x-source":"inject","x-form-label":"Sticky","x-form-type":"materialswitch"} */
	@Inject
	private String sticky;

	/* {"type":"object","x-source":"inject","x-form-type":"collection","x-form-label":"Links","properties":{"text":{"type":"string","x-source":"inject","x-form-label":"Link Text","x-form-type":"text"},"link":{"type":"string","x-source":"inject","x-form-label":"Link Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"}}} */
	@Inject
	private List<IComponent> links;

	/* {"type":"string","x-source":"inject","x-form-label":"Buttons","x-form-type":"collection","properties":{"buttontext":{"type":"string","x-source":"inject","x-form-label":"Button Text","x-form-type":"text"},"buttonlink":{"type":"string","x-source":"inject","x-form-label":"Button Link","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"},"buttoncolor":{"type":"string","x-source":"inject","x-form-label":"Button Color","x-form-type":"color"}}} */
	@Inject
	private List<IComponent> buttons;

	/* {"type":"string","x-source":"inject","x-form-label":"Block Color Scheme","x-form-type":"materialradio","x-default":"light","properties":{"light":{"x-form-name":"Light","x-form-value":"light"},"dark":{"x-form-name":"Dark","x-form-value":"dark"}}} */
	@Inject
	@Default(values ="light")
	private String colorscheme;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-default":"color","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"},"video":{"x-form-name":"Video","x-form-value":"video"}}} */
	@Inject
	@Default(values ="color")
	private String backgroundtype;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Video","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'video'","x-default":"https://www.youtube.com/embed/Ju86mknumYM","x-form-browserRoot":"/content/assets"} */
	@Inject
	@Default(values ="https://www.youtube.com/embed/Ju86mknumYM")
	private String bgvideo;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Image","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'image'","x-form-browserRoot":"/content/assets"} */
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

	/* {"type":"string","x-source":"inject","x-form-label":"Bottom Padding","x-form-type":"range","x-form-min":0,"x-form-max":150,"x-form-visible":"model.fullheight != 'true'"} */
	@Inject
	private String bottompadding;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Logo","x-form-type":"pathbrowser","x-form-browserRoot":"/content/assets"} */
	public String getLogo() {
		return logo;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Logo Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"} */
	public String getLogourl() {
		return logourl;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Logo Size","x-form-type":"range","x-form-min":1,"x-form-max":10} */
	public String getLogosize() {
		return logosize;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Collapse Menu","x-form-type":"materialswitch"} */
	public String getCollapsed() {
		return collapsed;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Sticky","x-form-type":"materialswitch"} */
	public String getSticky() {
		return sticky;
	}

	/* {"type":"object","x-source":"inject","x-form-type":"collection","x-form-label":"Links","properties":{"text":{"type":"string","x-source":"inject","x-form-label":"Link Text","x-form-type":"text"},"link":{"type":"string","x-source":"inject","x-form-label":"Link Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"}}} */
	public List<IComponent> getLinks() {
		return links;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Buttons","x-form-type":"collection","properties":{"buttontext":{"type":"string","x-source":"inject","x-form-label":"Button Text","x-form-type":"text"},"buttonlink":{"type":"string","x-source":"inject","x-form-label":"Button Link","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"},"buttoncolor":{"type":"string","x-source":"inject","x-form-label":"Button Color","x-form-type":"color"}}} */
	public List<IComponent> getButtons() {
		return buttons;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Block Color Scheme","x-form-type":"materialradio","x-default":"light","properties":{"light":{"x-form-name":"Light","x-form-value":"light"},"dark":{"x-form-name":"Dark","x-form-value":"dark"}}} */
	public String getColorscheme() {
		return colorscheme;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-default":"color","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"},"video":{"x-form-name":"Video","x-form-value":"video"}}} */
	public String getBackgroundtype() {
		return backgroundtype;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Video","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'video'","x-default":"https://www.youtube.com/embed/Ju86mknumYM","x-form-browserRoot":"/content/assets"} */
	public String getBgvideo() {
		return bgvideo;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Image","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'image'","x-form-browserRoot":"/content/assets"} */
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

	/* {"type":"string","x-source":"inject","x-form-label":"Bottom Padding","x-form-type":"range","x-form-min":0,"x-form-max":150,"x-form-visible":"model.fullheight != 'true'"} */
	public String getBottompadding() {
		return bottompadding;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
