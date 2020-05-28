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
    "Tabs": {
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
          "x-form-rows": 3,
          "x-form-type": "textarea"
        },
        "mediaposition": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Media Position",
          "x-form-type": "materialradio",
          "x-default": "before",
          "x-form-visible": "model.showmedia == 'true'",
          "properties": {
            "type1": {
              "x-form-name": "Before Content",
              "x-form-value": "before"
            },
            "type2": {
              "x-form-name": "After Content",
              "x-form-value": "after"
            }
          }
        },
        "mediaref": {
          "x-form-type": "reference",
          "type": "object",
          "x-type": "component",
          "properties": {
            "showmedia": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Show Media",
              "x-form-type": "materialswitch"
            },
            "mediatype": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Media type",
              "x-form-visible": "model.showmedia == 'true'",
              "x-form-type": "materialradio",
              "properties": {
                "image": {
                  "x-form-name": "Image",
                  "x-form-value": "image"
                },
                "video": {
                  "x-form-name": "Video",
                  "x-form-value": "video"
                },
                "icon": {
                  "x-form-name": "Icon",
                  "x-form-value": "icon"
                }
              }
            },
            "mediaicon": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Icon Chooser",
              "x-form-type": "iconbrowser",
              "x-form-hint": "Select an icon.",
              "x-form-validator": "required",
              "x-form-visible": "model.mediatype == 'icon' and model.showmedia == 'true'",
              "x-form-families": [
                "material",
                "font awesome"
              ]
            },
            "mediaiconsize": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Icon Size",
              "x-form-type": "materialrange",
              "x-form-visible": "model.mediatype == 'icon' and model.showmedia == 'true'",
              "x-default": 50,
              "x-form-min": 1,
              "x-form-max": 1000
            },
            "mediaiconcolor": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Icon Color",
              "x-form-type": "color",
              "x-form-visible": "model.mediatype == 'icon' and model.showmedia == 'true'",
              "x-default": "#000000"
            },
            "imagesrc": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Image Source",
              "x-form-visible": "model.mediatype == 'image' and model.showmedia == 'true'",
              "x-form-type": "pathbrowser",
              "x-form-browserRoot": "/content/themeclean/assets"
            },
            "imagealttext": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Image Alt Text",
              "x-form-visible": "model.mediatype == 'image' and model.showmedia == 'true'",
              "x-form-type": "text"
            },
            "videosrc": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Video Source",
              "x-form-visible": "model.mediatype == 'video' and model.showmedia == 'true'",
              "x-form-type": "pathbrowser",
              "x-form-browserRoot": "/content/themeclean/assets"
            },
            "mediawidth": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Width",
              "x-form-type": "materialrange",
              "x-form-visible": "model.showmedia == 'true'",
              "x-default": 100,
              "x-form-min": 10,
              "x-form-max": 100
            }
          }
        },
        "tabcolor": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Tab Color",
          "x-form-type": "materialselect",
          "x-default": "primary",
          "properties": {
            "primary": {
              "x-form-name": "Primary",
              "x-form-value": "primary"
            },
            "secondary": {
              "x-form-name": "Secondary",
              "x-form-value": "secondary"
            },
            "success": {
              "x-form-name": "Success",
              "x-form-value": "success"
            },
            "danger": {
              "x-form-name": "Danger",
              "x-form-value": "danger"
            },
            "warning": {
              "x-form-name": "Warning",
              "x-form-value": "warning"
            },
            "info": {
              "x-form-name": "Info",
              "x-form-value": "info"
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
        "tabs": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Tabs",
          "x-form-type": "collection",
          "properties": {
            "title": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Tab Title",
              "x-form-type": "text"
            },
            "text": {
              "type": "string",
              "x-source": "inject",
              "x-form-label": "Tab Text",
              "x-form-type": "texteditor"
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
  "name": "Tabs",
  "componentPath": "themeclean/components/tabs",
  "package": "com.themeclean.models",
  "modelName": "Tabs",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/tabs",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class TabsModel extends AbstractComponent {

    public TabsModel(Resource r) { super(r); }

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

	/* {"type":"string","x-source":"inject","x-form-label":"Subtitle","x-form-visible":"model.showsubtitle == 'true'","x-form-rows":3,"x-form-type":"textarea"} */
	@Inject
	private String subtitle;

	/* {"type":"string","x-source":"inject","x-form-label":"Media Position","x-form-type":"materialradio","x-default":"before","x-form-visible":"model.showmedia == 'true'","properties":{"type1":{"x-form-name":"Before Content","x-form-value":"before"},"type2":{"x-form-name":"After Content","x-form-value":"after"}}} */
	@Inject
	@Default(values ="before")
	private String mediaposition;

	/* {"type":"string","x-source":"inject","x-form-label":"Show Media","x-form-type":"materialswitch"} */
	@Inject
	private String showmedia;

	/* {"type":"string","x-source":"inject","x-form-label":"Media type","x-form-visible":"model.showmedia == 'true'","x-form-type":"materialradio","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"video":{"x-form-name":"Video","x-form-value":"video"},"icon":{"x-form-name":"Icon","x-form-value":"icon"}}} */
	@Inject
	private String mediatype;

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Chooser","x-form-type":"iconbrowser","x-form-hint":"Select an icon.","x-form-validator":"required","x-form-visible":"model.mediatype == 'icon' and model.showmedia == 'true'","x-form-families":["material","font awesome"]} */
	@Inject
	private String mediaicon;

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Size","x-form-type":"materialrange","x-form-visible":"model.mediatype == 'icon' and model.showmedia == 'true'","x-default":50,"x-form-min":1,"x-form-max":1000} */
	@Inject
	@Default(values ="50")
	private String mediaiconsize;

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Color","x-form-type":"color","x-form-visible":"model.mediatype == 'icon' and model.showmedia == 'true'","x-default":"#000000"} */
	@Inject
	@Default(values ="#000000")
	private String mediaiconcolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Image Source","x-form-visible":"model.mediatype == 'image' and model.showmedia == 'true'","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/assets"} */
	@Inject
	private String imagesrc;

	/* {"type":"string","x-source":"inject","x-form-label":"Image Alt Text","x-form-visible":"model.mediatype == 'image' and model.showmedia == 'true'","x-form-type":"text"} */
	@Inject
	private String imagealttext;

	/* {"type":"string","x-source":"inject","x-form-label":"Video Source","x-form-visible":"model.mediatype == 'video' and model.showmedia == 'true'","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/assets"} */
	@Inject
	private String videosrc;

	/* {"type":"string","x-source":"inject","x-form-label":"Width","x-form-type":"materialrange","x-form-visible":"model.showmedia == 'true'","x-default":100,"x-form-min":10,"x-form-max":100} */
	@Inject
	@Default(values ="100")
	private String mediawidth;

	/* {"type":"string","x-source":"inject","x-form-label":"Tab Color","x-form-type":"materialselect","x-default":"primary","properties":{"primary":{"x-form-name":"Primary","x-form-value":"primary"},"secondary":{"x-form-name":"Secondary","x-form-value":"secondary"},"success":{"x-form-name":"Success","x-form-value":"success"},"danger":{"x-form-name":"Danger","x-form-value":"danger"},"warning":{"x-form-name":"Warning","x-form-value":"warning"},"info":{"x-form-name":"Info","x-form-value":"info"},"light":{"x-form-name":"Light","x-form-value":"light"},"dark":{"x-form-name":"Dark","x-form-value":"dark"}}} */
	@Inject
	@Default(values ="primary")
	private String tabcolor;

	/* {"type":"string","x-source":"inject","x-form-label":"Tabs","x-form-type":"collection","properties":{"title":{"type":"string","x-source":"inject","x-form-label":"Tab Title","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Tab Text","x-form-type":"texteditor"}}} */
	@Inject
	private List<IComponent> tabs;

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

	/* {"type":"string","x-source":"inject","x-form-label":"Subtitle","x-form-visible":"model.showsubtitle == 'true'","x-form-rows":3,"x-form-type":"textarea"} */
	public String getSubtitle() {
		return subtitle;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Media Position","x-form-type":"materialradio","x-default":"before","x-form-visible":"model.showmedia == 'true'","properties":{"type1":{"x-form-name":"Before Content","x-form-value":"before"},"type2":{"x-form-name":"After Content","x-form-value":"after"}}} */
	public String getMediaposition() {
		return mediaposition;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Show Media","x-form-type":"materialswitch"} */
	public String getShowmedia() {
		return showmedia;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Media type","x-form-visible":"model.showmedia == 'true'","x-form-type":"materialradio","properties":{"image":{"x-form-name":"Image","x-form-value":"image"},"video":{"x-form-name":"Video","x-form-value":"video"},"icon":{"x-form-name":"Icon","x-form-value":"icon"}}} */
	public String getMediatype() {
		return mediatype;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Chooser","x-form-type":"iconbrowser","x-form-hint":"Select an icon.","x-form-validator":"required","x-form-visible":"model.mediatype == 'icon' and model.showmedia == 'true'","x-form-families":["material","font awesome"]} */
	public String getMediaicon() {
		return mediaicon;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Size","x-form-type":"materialrange","x-form-visible":"model.mediatype == 'icon' and model.showmedia == 'true'","x-default":50,"x-form-min":1,"x-form-max":1000} */
	public String getMediaiconsize() {
		return mediaiconsize;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Icon Color","x-form-type":"color","x-form-visible":"model.mediatype == 'icon' and model.showmedia == 'true'","x-default":"#000000"} */
	public String getMediaiconcolor() {
		return mediaiconcolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Image Source","x-form-visible":"model.mediatype == 'image' and model.showmedia == 'true'","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/assets"} */
	public String getImagesrc() {
		return imagesrc;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Image Alt Text","x-form-visible":"model.mediatype == 'image' and model.showmedia == 'true'","x-form-type":"text"} */
	public String getImagealttext() {
		return imagealttext;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Video Source","x-form-visible":"model.mediatype == 'video' and model.showmedia == 'true'","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/assets"} */
	public String getVideosrc() {
		return videosrc;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Width","x-form-type":"materialrange","x-form-visible":"model.showmedia == 'true'","x-default":100,"x-form-min":10,"x-form-max":100} */
	public String getMediawidth() {
		return mediawidth;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Tab Color","x-form-type":"materialselect","x-default":"primary","properties":{"primary":{"x-form-name":"Primary","x-form-value":"primary"},"secondary":{"x-form-name":"Secondary","x-form-value":"secondary"},"success":{"x-form-name":"Success","x-form-value":"success"},"danger":{"x-form-name":"Danger","x-form-value":"danger"},"warning":{"x-form-name":"Warning","x-form-value":"warning"},"info":{"x-form-name":"Info","x-form-value":"info"},"light":{"x-form-name":"Light","x-form-value":"light"},"dark":{"x-form-name":"Dark","x-form-value":"dark"}}} */
	public String getTabcolor() {
		return tabcolor;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Tabs","x-form-type":"collection","properties":{"title":{"type":"string","x-source":"inject","x-form-label":"Tab Title","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Tab Text","x-form-type":"texteditor"}}} */
	public List<IComponent> getTabs() {
		return tabs;
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
