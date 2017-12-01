package com.themeclean.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Pagelistnested": {
      "type": "object",
      "x-type": "component",
      "properties": {}
    }
  },
  "name": "Pagelistnested",
  "componentPath": "themeclean/components/pagelistnested",
  "package": "com.themeclean.models",
  "modelName": "Pagelistnested",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/pagelistnested",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PagelistnestedModel extends AbstractComponent {

    public PagelistnestedModel(Resource r) { super(r); }

    //GEN[:INJECT
    
//GEN]

    //GEN[:GETTERS
    
//GEN]

    //GEN[:CUSTOMGETTERS
	/*private Resource getCurrentPage(Resource resource) {
    	String resourceType = null;
    	try{
    		ValueMap props = resource.adaptTo(ValueMap.class);
		    resourceType = props.get("jcr:primaryType", "type not found");
		    // we only care about per:page node
		    if("per:Page".equals(resourceType)) {
		    	return resource;
		    }
		    else {
		    	return getCurrentPage(resource.getParent());
		    }
		} catch(Exception e){
    		return null;
		}
    }

	public PerPage getPage() {
		if (getCurrentPage(getResource()) != null) {
			PerPage perPage = getCurrentPage(getResource()).adaptTo(PerPage.class);
			return perPage;
		} else {
			return null;
		}
	}

	public List<PerPage> getChildrenPages() {
		List<PerPage> childList = new ArrayList<PerPage>();
		Iterator<PerPage> children = getPage().listChildren().iterator();
		while (children.hasNext()) {
			childList.add(children.next());
		}
		return childList;
	}*/
    //GEN]

}
