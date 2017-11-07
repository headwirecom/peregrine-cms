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
    "Textlinksvertical": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "title": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Links Section Title",
          "x-form-type": "text"
        },
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
              "x-form-type": "pathbrowser"
            }
          }
        }
      }
    }
  },
  "name": "Textlinksvertical",
  "componentPath": "themeclean/components/textlinksvertical",
  "package": "com.themeclean.models",
  "modelName": "Textlinksvertical",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/textlinksvertical",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class TextlinksverticalModel extends AbstractComponent {

    public TextlinksverticalModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Links Section Title","x-form-type":"text"} */
	@Inject
	private String title;

	/* {"type":"object","x-source":"inject","x-form-type":"collection","x-form-label":"Links","properties":{"text":{"type":"string","x-source":"inject","x-form-label":"Link Text","x-form-type":"text"},"link":{"type":"string","x-source":"inject","x-form-label":"Link Url","x-form-type":"pathbrowser"}}} */
	@Inject
	private List<IComponent> links;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Links Section Title","x-form-type":"text"} */
	public String getTitle() {
		return title;
	}

	/* {"type":"object","x-source":"inject","x-form-type":"collection","x-form-label":"Links","properties":{"text":{"type":"string","x-source":"inject","x-form-label":"Link Text","x-form-type":"text"},"link":{"type":"string","x-source":"inject","x-form-label":"Link Url","x-form-type":"pathbrowser"}}} */
	public List<IComponent> getLinks() {
		return links;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
