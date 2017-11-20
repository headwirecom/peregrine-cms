package com.themeclean.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.Container;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Breadcrumb": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "rootpath": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Root Path",
          "x-form-type": "pathbrowser",
          "x-form-browserRoot": "/content/sites"
        }
      }
    }
  },
  "name": "Breadcrumb",
  "componentPath": "themeclean/components/breadcrumb",
  "package": "com.themeclean.models",
  "modelName": "Breadcrumb",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/breadcrumb",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class BreadcrumbModel extends AbstractComponent {
	
	public BreadcrumbModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-label":"Root Path","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"} */
	@Inject
	private String rootpath;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-label":"Root Path","x-form-type":"pathbrowser","x-form-browserRoot":"/content/sites"} */
	public String getRootpath() {
		return rootpath;
	}

public List<TextLink> links;
	
	/* Method to recursively get child page links, given a root page path */
    public List<TextLink> getLinks(){
    	
    	links = new ArrayList<TextLink>();
    	return getDeepLinks(getResource());
    	
    }
    
    private List<TextLink> getDeepLinks(Resource resource){
    	
    	try{
			    		
    		ValueMap props = resource.adaptTo(ValueMap.class);
		    String resourceType = props.get("jcr:primaryType", "type not found");
		    // we only care about per:page child
		    if(resourceType.equals("per:Page")){
			    TextLink link = new TextLink(resource.getPath(), getPageTitle(resource.getPath()));
			    links.add(0,link);
		    }
		    // move on to its parent resource
		    if(resource.getParent() != null) {
		    	getDeepLinks(resource.getParent());
		    }
    	} catch(Exception e){
			e.printStackTrace();
		}
    	
    	return links;
    	
    	
    }
    	
	private String getPageTitle(String pageUrl){
		try{
			String resourcePath = pageUrl + "/jcr:content";
			ResourceResolver resourceResolver = getResource().getResourceResolver();
			ValueMap props = resourceResolver.getResource(resourcePath).adaptTo(ValueMap.class);
			return props.get("jcr:title", "title not found");
		} catch(Exception e){
			e.printStackTrace();
			return "title not found....";
		}
	}
    
	private class TextLink {
		
		public TextLink(String link, String text){
			this.link = link;
			this.text = text;
		}
		private String link;
		private String text;
		
		public String getLink(){
			return link;
		}
		
		public String getText(){
			return text;
		}
	}

//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}
