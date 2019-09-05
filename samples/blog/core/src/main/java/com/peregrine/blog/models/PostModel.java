package com.peregrine.blog.models;

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Post": {
      "type": "object",
      "x-type": "container",
      "properties": {
        "title": {
          "type": "string",
          "x-source": "inject"
        },
        "date": {
          "type": "string",
          "x-source": "inject"
        },
        "author": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  },
  "name": "Post",
  "componentPath": "blog/components/post",
  "package": "com.peregrine.blog.models",
  "modelName": "Post",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "blog/components/post",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PostModel extends Container {

    public PostModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String title;

	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String date;

	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String author;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject"} */
	public String getTitle() {
		return title;
	}

	/* {"type":"string","x-source":"inject"} */
	public String getDate() {
		return date;
	}

	/* {"type":"string","x-source":"inject"} */
	public String getAuthor() {
		return author;
	}


//GEN]

}
