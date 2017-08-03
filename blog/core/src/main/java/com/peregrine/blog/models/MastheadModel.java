package com.peregrine.blog.models;

import com.peregrine.adaption.PerPage;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Masthead": {
      "type": "object",
      "x-type": "component",
      "properties": {}
    }
  },
  "name": "Masthead",
  "componentPath": "post/components/masthead",
  "package": "com.post.models",
  "modelName": "Masthead",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "blog/components/masthead",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class MastheadModel extends AbstractComponent {

    public MastheadModel(Resource r) { super(r); }

    //GEN[:INJECT
    
//GEN]

    //GEN[:GETTERS
    
//GEN]

    public List getNavigation() {
        ArrayList ret = new ArrayList();
        PerPage page = getResource().adaptTo(PerPage.class);

        String path = page.getPath();
        String[] segments = path.split("/");
        segments[2] = "sites";
        String homePagePath = String.join("/", Arrays.copyOf(segments, 4));

        Resource homePage = getResource().getResourceResolver().getResource(homePagePath);

        Iterable<Resource> children = homePage.getChildren();
        for (Resource child: children) {
            if(!child.getName().equals("jcr:content")) {
                boolean hideInNav = child.getChild("jcr:content").getValueMap().get("hideInNav", false);
                if(!hideInNav) {
                    ret.add(new NavItem(child));
                }
            }
        }

        return ret;
    }

    static class NavItem {

        private final Resource resource;

        public NavItem(Resource resource) {
            this.resource = resource;
        }

        public String getPath() {
            return resource.getPath();
        }

        public String getTitle() {
            return resource.getName();
        }
    }
}
