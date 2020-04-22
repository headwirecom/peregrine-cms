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
    "Carousel": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "carouselheight": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Carousel Height",
          "x-form-type": "materialrange",
          "x-default": 80,
          "x-form-min": 50,
          "x-form-max": 100
        },
        "autoplay": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Auto Play",
          "x-form-type": "materialswitch"
        },
        "interval": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Interval",
          "x-form-type": "materialrange",
          "x-form-visible": "model.autoplay == 'true'",
          "x-default": 5,
          "x-form-min": 1,
          "x-form-max": 20
        },
        "pause": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Pause On Hover",
          "x-form-type": "materialswitch",
          "x-form-visible": "model.autoplay == 'true'",
          "x-default": "false"
        },
        "wrap": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Wrap",
          "x-form-type": "materialswitch",
          "x-default": "true"
        },
        "indicators": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Indicators",
          "x-form-type": "materialswitch",
          "x-default": "true"
        },
        "controls": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Controls",
          "x-form-type": "materialswitch",
          "x-default": "true"
        },
        "keyboard": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Keyboard",
          "x-form-type": "materialswitch",
          "x-default": "true"
        },
        "captionbg": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Caption Background",
          "x-form-type": "materialswitch"
        },
        "slides": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Slides",
          "x-form-fieldLabel": "heading",
          "x-form-type": "collection",
          "properties": {
            "imagepath": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Image Source",
              "x-form-type": "pathbrowser",
              "x-form-browserRoot": "/content/themeclean/assets"
            },
            "heading": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Image Heading",
              "x-form-type": "text"
            },
            "text": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Image Text",
              "x-form-type": "texteditor"
            },
            "alt": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Image Alt Text",
              "x-form-type": "text"
            }
          }
        },
        "bgref": {
          "x-form-type": "reference",
          "type": "object",
          "x-type": "component",
          "properties": {
            "anchorname": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Anchor Name",
              "x-form-type": "text"
            },
            "colorscheme": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Block Color Scheme",
              "x-form-type": "materialradio",
              "x-default": "",
              "properties": {
                "none": {
                  "x-form-name": "None",
                  "x-form-value": ""
                },
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
            "custombackground": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Custom Background",
              "x-form-type": "materialswitch",
              "x-default": "false"
            },
            "backgroundtype": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Type",
              "x-form-type": "materialradio",
              "x-form-visible": "model.custombackground == 'true'",
              "properties": {
                "color": {
                  "x-form-name": "Color",
                  "x-form-value": "color"
                },
                "gradient": {
                  "x-form-name": "Gradient",
                  "x-form-value": "gradient"
                },
                "image": {
                  "x-form-name": "Image",
                  "x-form-value": "image"
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
              "x-form-visible": "model.backgroundtype == 'video' and model.custombackground == 'true'",
              "x-default": "https://www.youtube.com/embed/Ju86mknumYM",
              "x-form-browserRoot": "/content/themeclean/assets"
            },
            "bgimage": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Image",
              "x-form-type": "pathbrowser",
              "x-form-visible": "model.backgroundtype == 'image' and model.custombackground == 'true'",
              "x-form-browserRoot": "/content/themeclean/assets"
            },
            "overlay": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Overlay",
              "x-form-type": "materialswitch",
              "x-form-visible": "model.backgroundtype == 'image' and model.custombackground == 'true'"
            },
            "overlaycolor": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Overlay Color",
              "x-form-type": "color",
              "x-form-visible": "model.overlay == 'true' and model.backgroundtype == 'image' and model.custombackground == 'true'",
              "x-default": "#ffffff"
            },
            "overlayopacity": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Overlay opacity",
              "x-form-type": "materialrange",
              "x-form-min": 0,
              "x-form-max": 100,
              "x-form-visible": "model.overlay == 'true' and model.backgroundtype == 'image' and model.custombackground == 'true'",
              "x-default": "50"
            },
            "bgcolor": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Background Color",
              "x-form-type": "color",
              "x-form-visible": "(model.backgroundtype == 'color' or model.backgroundtype == 'gradient') and model.custombackground == 'true'",
              "x-default": "#ffffff"
            },
            "color2": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Color 2",
              "x-form-type": "color",
              "x-form-visible": "model.backgroundtype == 'gradient' and model.custombackground == 'true'",
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
              "x-form-type": "materialrange",
              "x-form-min": 0,
              "x-form-max": 150,
              "x-form-visible": "model.fullheight != 'true'"
            },
            "bottompadding": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Bottom Padding",
              "x-form-type": "materialrange",
              "x-form-min": 0,
              "x-form-max": 150,
              "x-form-visible": "model.fullheight != 'true'"
            }
          }
        }
      }
    }
  },
  "name": "Carousel",
  "componentPath": "themeclean/components/carousel",
  "package": "com.themeclean.models",
  "modelName": "Carousel",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/carousel",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class CarouselModel extends AbstractComponent {

    public CarouselModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Carousel Height","x-form-type":"materialrange","x-default":80,"x-form-min":50,"x-form-max":100} */
	@Inject
	@Default(values ="80")
	private String carouselheight;

	/* {"type":"string","x-source":"inject","x-form-label":"Auto Play","x-form-type":"materialswitch"} */
	@Inject
	private String autoplay;

	/* {"type":"string","x-source":"inject","x-form-label":"Interval","x-form-type":"materialrange","x-form-visible":"model.autoplay == 'true'","x-default":5,"x-form-min":1,"x-form-max":20} */
	@Inject
	@Default(values ="5")
	private String interval;

	/* {"type":"string","x-source":"inject","x-form-label":"Pause On Hover","x-form-type":"materialswitch","x-form-visible":"model.autoplay == 'true'","x-default":"false"} */
	@Inject
	@Default(values ="false")
	private String pause;

	/* {"type":"string","x-source":"inject","x-form-label":"Wrap","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String wrap;

	/* {"type":"string","x-source":"inject","x-form-label":"Indicators","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String indicators;

	/* {"type":"string","x-source":"inject","x-form-label":"Controls","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String controls;

	/* {"type":"string","x-source":"inject","x-form-label":"Keyboard","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String keyboard;

	/* {"type":"string","x-source":"inject","x-form-label":"Caption Background","x-form-type":"materialswitch"} */
	@Inject
	private String captionbg;

	/* {"type":"string","x-source":"inject","x-form-label":"Slides","x-form-fieldLabel":"heading","x-form-type":"collection","properties":{"imagepath":{"type":"string","x-source":"inject","x-form-label":"Image Source","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/assets"},"heading":{"type":"string","x-source":"inject","x-form-label":"Image Heading","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Image Text","x-form-type":"texteditor"},"alt":{"type":"string","x-source":"inject","x-form-label":"Image Alt Text","x-form-type":"text"}}} */
	@Inject
	private List<IComponent> slides;

	/* {"type":"string","x-source":"inject","x-form-label":"Anchor Name","x-form-type":"text"} */
	@Inject
	private String anchorname;

	/* {"type":"string","x-source":"inject","x-form-label":"Block Color Scheme","x-form-type":"materialradio","x-default":"","properties":{"none":{"x-form-name":"None","x-form-value":""},"light":{"x-form-name":"Light","x-form-value":"light"},"dark":{"x-form-name":"Dark","x-form-value":"dark"}}} */
	@Inject
	@Default(values ="")
	private String colorscheme;

	/* {"type":"string","x-source":"inject","x-form-label":"Custom Background","x-form-type":"materialswitch","x-default":"false"} */
	@Inject
	@Default(values ="false")
	private String custombackground;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-form-visible":"model.custombackground == 'true'","properties":{"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"},"image":{"x-form-name":"Image","x-form-value":"image"},"video":{"x-form-name":"Video","x-form-value":"video"}}} */
	@Inject
	private String backgroundtype;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Video","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'video' and model.custombackground == 'true'","x-default":"https://www.youtube.com/embed/Ju86mknumYM","x-form-browserRoot":"/content/themeclean/assets"} */
	@Inject
	@Default(values ="https://www.youtube.com/embed/Ju86mknumYM")
	private String bgvideo;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Image","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'image' and model.custombackground == 'true'","x-form-browserRoot":"/content/themeclean/assets"} */
	@Inject
	private String bgimage;

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay","x-form-type":"materialswitch","x-form-visible":"model.backgroundtype == 'image' and model.custombackground == 'true'"} */
	@Inject
	private String overlay;

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay Color","x-form-type":"color","x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image' and model.custombackground == 'true'","x-default":"#ffffff"} */
	@Inject
	@Default(values ="#ffffff")
	private String overlaycolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay opacity","x-form-type":"materialrange","x-form-min":0,"x-form-max":100,"x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image' and model.custombackground == 'true'","x-default":"50"} */
	@Inject
	@Default(values ="50")
	private String overlayopacity;

	/* {"type":"string","x-source":"inject","x-form-label":"Background Color","x-form-type":"color","x-form-visible":"(model.backgroundtype == 'color' or model.backgroundtype == 'gradient') and model.custombackground == 'true'","x-default":"#ffffff"} */
	@Inject
	@Default(values ="#ffffff")
	private String bgcolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Color 2","x-form-type":"color","x-form-visible":"model.backgroundtype == 'gradient' and model.custombackground == 'true'","x-default":"#c0c0c0"} */
	@Inject
	@Default(values ="#c0c0c0")
	private String color2;

	/* {"type":"string","x-source":"inject","x-form-label":"Full Width","x-form-type":"materialswitch"} */
	@Inject
	private String fullwidth;

	/* {"type":"string","x-source":"inject","x-form-label":"Full Height","x-form-type":"materialswitch"} */
	@Inject
	private String fullheight;

	/* {"type":"string","x-source":"inject","x-form-label":"Top Padding","x-form-type":"materialrange","x-form-min":0,"x-form-max":150,"x-form-visible":"model.fullheight != 'true'"} */
	@Inject
	private String toppadding;

	/* {"type":"string","x-source":"inject","x-form-label":"Bottom Padding","x-form-type":"materialrange","x-form-min":0,"x-form-max":150,"x-form-visible":"model.fullheight != 'true'"} */
	@Inject
	private String bottompadding;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Carousel Height","x-form-type":"materialrange","x-default":80,"x-form-min":50,"x-form-max":100} */
	public String getCarouselheight() {
		return carouselheight;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Auto Play","x-form-type":"materialswitch"} */
	public String getAutoplay() {
		return autoplay;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Interval","x-form-type":"materialrange","x-form-visible":"model.autoplay == 'true'","x-default":5,"x-form-min":1,"x-form-max":20} */
	public String getInterval() {
		return interval;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Pause On Hover","x-form-type":"materialswitch","x-form-visible":"model.autoplay == 'true'","x-default":"false"} */
	public String getPause() {
		return pause;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Wrap","x-form-type":"materialswitch","x-default":"true"} */
	public String getWrap() {
		return wrap;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Indicators","x-form-type":"materialswitch","x-default":"true"} */
	public String getIndicators() {
		return indicators;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Controls","x-form-type":"materialswitch","x-default":"true"} */
	public String getControls() {
		return controls;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Keyboard","x-form-type":"materialswitch","x-default":"true"} */
	public String getKeyboard() {
		return keyboard;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Caption Background","x-form-type":"materialswitch"} */
	public String getCaptionbg() {
		return captionbg;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Slides","x-form-fieldLabel":"heading","x-form-type":"collection","properties":{"imagepath":{"type":"string","x-source":"inject","x-form-label":"Image Source","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/assets"},"heading":{"type":"string","x-source":"inject","x-form-label":"Image Heading","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Image Text","x-form-type":"texteditor"},"alt":{"type":"string","x-source":"inject","x-form-label":"Image Alt Text","x-form-type":"text"}}} */
	public List<IComponent> getSlides() {
		return slides;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Anchor Name","x-form-type":"text"} */
	public String getAnchorname() {
		return anchorname;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Block Color Scheme","x-form-type":"materialradio","x-default":"","properties":{"none":{"x-form-name":"None","x-form-value":""},"light":{"x-form-name":"Light","x-form-value":"light"},"dark":{"x-form-name":"Dark","x-form-value":"dark"}}} */
	public String getColorscheme() {
		return colorscheme;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Custom Background","x-form-type":"materialswitch","x-default":"false"} */
	public String getCustombackground() {
		return custombackground;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Type","x-form-type":"materialradio","x-form-visible":"model.custombackground == 'true'","properties":{"color":{"x-form-name":"Color","x-form-value":"color"},"gradient":{"x-form-name":"Gradient","x-form-value":"gradient"},"image":{"x-form-name":"Image","x-form-value":"image"},"video":{"x-form-name":"Video","x-form-value":"video"}}} */
	public String getBackgroundtype() {
		return backgroundtype;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Video","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'video' and model.custombackground == 'true'","x-default":"https://www.youtube.com/embed/Ju86mknumYM","x-form-browserRoot":"/content/themeclean/assets"} */
	public String getBgvideo() {
		return bgvideo;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Image","x-form-type":"pathbrowser","x-form-visible":"model.backgroundtype == 'image' and model.custombackground == 'true'","x-form-browserRoot":"/content/themeclean/assets"} */
	public String getBgimage() {
		return bgimage;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay","x-form-type":"materialswitch","x-form-visible":"model.backgroundtype == 'image' and model.custombackground == 'true'"} */
	public String getOverlay() {
		return overlay;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay Color","x-form-type":"color","x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image' and model.custombackground == 'true'","x-default":"#ffffff"} */
	public String getOverlaycolor() {
		return overlaycolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Overlay opacity","x-form-type":"materialrange","x-form-min":0,"x-form-max":100,"x-form-visible":"model.overlay == 'true' and model.backgroundtype == 'image' and model.custombackground == 'true'","x-default":"50"} */
	public String getOverlayopacity() {
		return overlayopacity;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Background Color","x-form-type":"color","x-form-visible":"(model.backgroundtype == 'color' or model.backgroundtype == 'gradient') and model.custombackground == 'true'","x-default":"#ffffff"} */
	public String getBgcolor() {
		return bgcolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Color 2","x-form-type":"color","x-form-visible":"model.backgroundtype == 'gradient' and model.custombackground == 'true'","x-default":"#c0c0c0"} */
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

	/* {"type":"string","x-source":"inject","x-form-label":"Top Padding","x-form-type":"materialrange","x-form-min":0,"x-form-max":150,"x-form-visible":"model.fullheight != 'true'"} */
	public String getToppadding() {
		return toppadding;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Bottom Padding","x-form-type":"materialrange","x-form-min":0,"x-form-max":150,"x-form-visible":"model.fullheight != 'true'"} */
	public String getBottompadding() {
		return bottompadding;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
