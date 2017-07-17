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
    "Pagination": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "previous": {
          "type": "string",
          "x-source": "inject"
        },
        "next": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  },
  "name": "Pagination",
  "componentPath": "post/components/pagination",
  "package": "com.post.models",
  "modelName": "Pagination",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "post/components/pagination",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PaginationModel extends AbstractComponent {

    public PaginationModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String previous;

	/* {"type":"string","x-source":"inject"} */
	@Inject
	private String next;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject"} */
	public String getPrevious() {
		return previous;
	}

	/* {"type":"string","x-source":"inject"} */
	public String getNext() {
		return next;
	}


//GEN]

}
