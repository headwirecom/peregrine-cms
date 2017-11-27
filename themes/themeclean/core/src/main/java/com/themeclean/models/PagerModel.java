package com.themeclean.models;

import com.peregrine.adaption.PerPage;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.Container;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Pager": {
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
  "name": "Pager",
  "componentPath": "themeclean/components/pager",
  "package": "com.themeclean.models",
  "modelName": "Pager",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/pager",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PagerModel extends AbstractComponent {

	private static final Logger LOG = LoggerFactory.getLogger(PagerModel.class);
	
    public PagerModel(Resource r) { super(r); }

    public String getPrevious() {
        PerPage page = getCurrentPage(getResource()).adaptTo(PerPage.class);
        if(page == null) return "not adaptable";
        PerPage prev = page.getPrevious();
        return prev != null ? prev.getPath(): "unknown";
    }

    public String getNext() {
        PerPage page = getCurrentPage(getResource()).adaptTo(PerPage.class);
        if(page == null) return "not adaptable";
        PerPage next = page.getNext();
        return next != null ? next.getPath(): "unknown";
    }
    
    private Resource getCurrentPage(Resource resource) {
    	String resourceType = null;
    	try{
    		
    		ValueMap props = resource.adaptTo(ValueMap.class);
		    resourceType = props.get("jcr:primaryType", "type not found");
		    LOG.debug("resource type is: " + resourceType + "  path is:" + resource.getPath());
		    // we only care about per:page node
		    if("per:Page".equals(resourceType)) {
		    	LOG.debug("returned resource type is: " + resourceType + "  path is:" + resource.getPath());
		    	return resource;
		    }
		    else {
		    	return getCurrentPage(resource.getParent());
		    }
		} catch(Exception e){
    		LOG.error("Exception: " + e);
    		return null;
		}
    	
    }


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
