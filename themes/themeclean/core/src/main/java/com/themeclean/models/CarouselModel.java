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
        "text": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "texteditor"
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
    	/* {"type":"string","x-source":"inject","x-form-type":"texteditor"} */
    @Inject @Default(values = "5000")
    private String interval;

    @Inject @Default(values = "hover")
    private String pause;

    @Inject @Default(values = "true")
    private String ride;

    @Inject @Default(values = "true")
    private String indicators;

    @Inject @Default(values = "true")
    private String controls;

    @Inject @Default(values = "true")
    private String wrap;

    @Inject @Default(values = "true")
    private String keyboard;
    
    @Inject
	private List<IComponent> slides;

    public String getInterval() {
        return interval;
    }

    public String getPause() {
        return pause;
    }

    public String getRide() {
        return ride;
    }

    public String getControls() {
        return controls;
    }

    public String getIndicators() {
        return indicators;
    }

    public String getWrap() {
        return wrap;
    }

    public String getKeyboard() {
        return keyboard;
    }

    public List<IComponent> getSlides() {
		return slides;
	}

//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
