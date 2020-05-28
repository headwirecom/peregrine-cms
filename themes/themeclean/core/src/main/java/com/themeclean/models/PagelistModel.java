package com.themeclean.models;

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
          "x-form-browserRoot": "/content/themeclean/pages"
        },
        "includeroot": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Include Root",
          "x-form-type": "materialswitch",
          "x-form-default": false
        },
        "levels": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "number",
          "x-form-label": "Levels",
          "x-form-default": 1,
          "x-form-min": 1
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
    	/* {"type":"string","x-source":"inject","x-form-type":"pathbrowser","x-form-label":"Root Page","x-form-browserRoot":"/content/themeclean/pages"} */
	@Inject
	private String rootpage;

	/* {"type":"string","x-source":"inject","x-form-label":"Include Root","x-form-type":"materialswitch","x-form-default":false} */
	@Inject
	private String includeroot;

	/* {"type":"string","x-source":"inject","x-form-type":"number","x-form-label":"Levels","x-form-default":1,"x-form-min":1} */
	@Inject
	private String levels;


//GEN]

    //GEN[:GETTERS
    	/* {"type":"string","x-source":"inject","x-form-type":"pathbrowser","x-form-label":"Root Page","x-form-browserRoot":"/content/themeclean/pages"} */
	public String getRootpage() {
		return rootpage;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Include Root","x-form-type":"materialswitch","x-form-default":false} */
	public String getIncluderoot() {
		return includeroot;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
	private static final Logger LOG = LoggerFactory.getLogger(PagelistModel.class);

	/* {"type":"string","x-source":"inject","x-form-type":"number","x-form-label":"Levels","x-form-default":1,"x-form-min":1} */
	public String getLevels() {
		return levels == null ? "1" : levels;
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
		String rootPage = getRootpage();
		if (rootPage != null) {
			int levels = Integer.parseInt(getLevels());
			PerPageManager ppm = getResource().getResourceResolver().adaptTo(PerPageManager.class);
			PerPage page = ppm.getPage(getRootpage());
			if (page != null) {
				for (PerPage child : page.listChildren()) {
					if (!child.getPath().equals(page.getPath())) {
						childPages.add(new Page(child, levels));
					}
				}
			}
		}
		return childPages;
	}

	class Page {

	private PerPage page;
	private int levels;

	public Page(PerPage page) {
		this.page = page;
	}

	public Page(PerPage page, int levels) {
		this.page = page;
		this.levels = levels;
	}

	public String getTitle() {
		return page.getTitle();
	}

	public String getPath() {
		return page.getPath();
	}

	public int getLevels() {
		return levels;
	}

	public Boolean getHasChildren() {
		return levels <= 1 ? false : true;
	}

	public List<Page> getChildrenPages() {
		List<Page> childPages = new ArrayList<Page>();
		System.out.println();
		if(page != null) {
			for (PerPage child: page.listChildren()) {
				if(!child.getPath().equals(page.getPath())) {
					childPages.add(new Page(child, levels-1));
				}
			}
		}
		return childPages;
	}
}
    //GEN]

}
