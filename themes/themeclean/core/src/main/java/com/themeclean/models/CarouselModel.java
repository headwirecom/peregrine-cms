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
    "Carousel": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "interval": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Interval",
          "x-form-type": "number",
          "x-default": "5000"
        },
        "pause": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Pause",
          "x-form-type": "text",
          "x-default": "pause"
        },
        "ride": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Ride",
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
        "wrap": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Wrap",
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
              "x-form-browserRoot": "/content/assets"
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
    	/* {"type":"string","x-source":"inject","x-form-label":"Interval","x-form-type":"number","x-default":"5000"} */
	@Inject
	@Default(values ="5000")
	private String interval;

	/* {"type":"string","x-source":"inject","x-form-label":"Pause","x-form-type":"text","x-default":"pause"} */
	@Inject
	@Default(values ="pause")
	private String pause;

	/* {"type":"string","x-source":"inject","x-form-label":"Ride","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String ride;

	/* {"type":"string","x-source":"inject","x-form-label":"Indicators","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String indicators;

	/* {"type":"string","x-source":"inject","x-form-label":"Controls","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String controls;

	/* {"type":"string","x-source":"inject","x-form-label":"Wrap","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String wrap;

	/* {"type":"string","x-source":"inject","x-form-label":"Keyboard","x-form-type":"materialswitch","x-default":"true"} */
	@Inject
	@Default(values ="true")
	private String keyboard;

	/* {"type":"string","x-source":"inject","x-form-label":"Slides","x-form-fieldLabel":"heading","x-form-type":"collection","properties":{"imagepath":{"type":"string","x-source":"inject","x-form-label":"Image Source","x-form-type":"pathbrowser","x-form-browserRoot":"/content/assets"},"heading":{"type":"string","x-source":"inject","x-form-label":"Image Heading","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Image Text","x-form-type":"texteditor"},"alt":{"type":"string","x-source":"inject","x-form-label":"Image Alt Text","x-form-type":"text"}}} */
	@Inject
	private List<IComponent> slides;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Interval","x-form-type":"number","x-default":"5000"} */
	public String getInterval() {
		return interval;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Pause","x-form-type":"text","x-default":"pause"} */
	public String getPause() {
		return pause;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Ride","x-form-type":"materialswitch","x-default":"true"} */
	public String getRide() {
		return ride;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Indicators","x-form-type":"materialswitch","x-default":"true"} */
	public String getIndicators() {
		return indicators;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Controls","x-form-type":"materialswitch","x-default":"true"} */
	public String getControls() {
		return controls;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Wrap","x-form-type":"materialswitch","x-default":"true"} */
	public String getWrap() {
		return wrap;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Keyboard","x-form-type":"materialswitch","x-default":"true"} */
	public String getKeyboard() {
		return keyboard;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Slides","x-form-fieldLabel":"heading","x-form-type":"collection","properties":{"imagepath":{"type":"string","x-source":"inject","x-form-label":"Image Source","x-form-type":"pathbrowser","x-form-browserRoot":"/content/assets"},"heading":{"type":"string","x-source":"inject","x-form-label":"Image Heading","x-form-type":"text"},"text":{"type":"string","x-source":"inject","x-form-label":"Image Text","x-form-type":"texteditor"},"alt":{"type":"string","x-source":"inject","x-form-label":"Image Alt Text","x-form-type":"text"}}} */
	public List<IComponent> getSlides() {
		return slides;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
