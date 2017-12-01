package com.themeclean.models;

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Pagelist": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "rootpage": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "pathbrowser",
          "x-form-label": "Root Page",
          "x-form-browserRoot": "/content/sites"
        },
        "includeroot": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Include Root",
          "x-form-type": "materialcheckbox",
          "x-form-default": false
        },
        "levels": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "number",
          "x-form-label": "Levels",
          "x-form-default": 1
        }
      }
    }
  },
  "name": "Pagelist",
  "componentPath": "themeclean/components/pagelist",
  "package": "com.themeclean.models",
  "modelName": "Pagelist",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "themeclean/components/pagelist",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PagelistModel extends AbstractComponent {

    public PagelistModel(Resource r) { super(r); }

    //GEN[:INJECT
    	/* {"type":"string","x-source":"inject","x-form-type":"pathbrowser","x-form-label":"Root Page","x-form-browserRoot":"/content/sites"} */
	@Inject
	private String rootpage;

	/* {"type":"string","x-source":"inject","x-form-label":"Include Root","x-form-type":"materialcheckbox","x-form-default":false} */
	@Inject
	private String includeroot;

	/* {"type":"string","x-source":"inject","x-form-type":"number","x-form-label":"Levels","x-form-default":1} */
	@Inject
	private String levels;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-type":"pathbrowser","x-form-label":"Root Page","x-form-browserRoot":"/content/sites"} */
	public String getRootpage() {
		return rootpage;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Include Root","x-form-type":"materialcheckbox","x-form-default":false} */
	public String getIncluderoot() {
		return includeroot;
	}

	/* {"type":"string","x-source":"inject","x-form-type":"number","x-form-label":"Levels","x-form-default":1} */
	public String getLevels() {
		return levels;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
	private static final Logger LOG = LoggerFactory.getLogger(PagelistModel.class);
	private Resource getCurrentPage(Resource resource) {
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
    		LOG.error("Exception: " + e);
    		return null;
		}
    }

	public String getRootPageTitle() {
		PerPageManager ppm = getResource().getResourceResolver().adaptTo(PerPageManager.class);
		PerPage page = ppm.getPage(getRootpage());
        if(page == null) return "not adaptable";
        return page != null ? page.getTitle(): "";
	}

	public String getRootPageLink() {
		PerPageManager ppm = getResource().getResourceResolver().adaptTo(PerPageManager.class);
		PerPage page = ppm.getPage(getRootpage());
        if(page == null) return "not adaptable";
        return page != null ? page.getPath(): "";
	}

	public List<Page> getChildrenPages() {
		List<Page> childPages = new ArrayList<Page>();
		PerPage page = getCurrentPage(getResource()).adaptTo(PerPage.class);
		if(page != null) {
			for (PerPage child: page.listChildren()) {
				if(!child.getPath().equals(page.getPath())) {
					childPages.add(new Page(child));
				}
			}
		}
		return childPages;
	}
    //GEN]

}

class Page {

	private PerPage page;

	public Page(PerPage page) {
		this.page = page;
	}

	public String getTitle() {
		return page.getTitle();
	}

	public String getPath() {
		return page.getPath();
	}

	public List<Page> getChildrenPages() {
		List<Page> childPages = new ArrayList<Page>();
		System.out.println();
		if(page != null) {
			for (PerPage child: page.listChildren()) {
				if(!child.getPath().equals(page.getPath())) {
					childPages.add(new Page(child));
				}
			}
		}
		return childPages;
	}

}
