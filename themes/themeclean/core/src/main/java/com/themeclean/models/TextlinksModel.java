package com.themeclean.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import java.util.List;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Textlinks": {
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
              "x-form-browserRoot": "/content/themeclean/pages"
            }
          }
        }
      }
    }
  },
  "name": "Textlinks",
  "componentPath": "themeclean/components/textlinks",
  "package": "com.themeclean.models",
  "modelName": "Textlinks",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/textlinks",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class TextlinksModel extends AbstractComponent {

    public TextlinksModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"object","x-source":"inject","x-form-type":"collection","x-form-label":"Links","properties":{"text":{"type":"string","x-source":"inject","x-form-label":"Link Text","x-form-type":"text"},"link":{"type":"string","x-source":"inject","x-form-label":"Link Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/pages"}}} */
	@Inject
	private List<IComponent> links;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"object","x-source":"inject","x-form-type":"collection","x-form-label":"Links","properties":{"text":{"type":"string","x-source":"inject","x-form-label":"Link Text","x-form-type":"text"},"link":{"type":"string","x-source":"inject","x-form-label":"Link Url","x-form-type":"pathbrowser","x-form-browserRoot":"/content/themeclean/pages"}}} */
	public List<IComponent> getLinks() {
		return links;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
