package com.blog.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.Container;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Sidebar": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "title": {
          "type": "string",
          "x-source": "inject"
        },
        "text": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "texteditor"
        },
        "style": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  },
  "name": "Sidebar",
  "componentPath": "post/components/sidebar",
  "package": "com.post.models",
  "modelName": "Sidebar",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "post/components/sidebar",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class SidebarModel extends AbstractComponent {

    public SidebarModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String title;

	/* {"type":"string","x-source":"inject","x-form-type":"texteditor"} */
	@Inject
	private String text;

	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String style;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject"} */
	public String getTitle() {
		return title;
	}

	/* {"type":"string","x-source":"inject","x-form-type":"texteditor"} */
	public String getText() {
		return text;
	}

	/* {"type":"string","x-source":"inject"} */
	public String getStyle() {
		return style;
	}


//GEN]

}
