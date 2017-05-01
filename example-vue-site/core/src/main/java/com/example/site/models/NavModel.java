package com.example.site.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rr on 4/18/2017.
 */
@Model(adaptables = Resource.class, resourceType = "example/components/nav", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class NavModel extends AbstractComponent {

    @Inject @Default(values = "")
    private String brand;

    public NavModel(Resource resource) {
        super(resource);
    }

    public String getBrand() {
        return brand == null ? "" : brand;
    }

    public List<NavItem> getNavigation() {
        List<NavItem> ret = new ArrayList<>();

        Resource homePage = getResource().getResourceResolver().getResource("/content/sites/example");
        Iterator<Resource> children = homePage.listChildren();

        for (Iterator<Resource> it = children; it.hasNext(); ) {
            Resource child = it.next();
            if("per:Page".equals(child.getResourceType())) {

                Resource content = child.getChild("jcr:content");
                ValueMap map = content.adaptTo(ValueMap.class);
                ret.add(new NavItem(child.getPath(), ""+map.get("jcr:title")));
            }
        }
        return ret;
    }

    static class NavItem {

        private String path;
        private String title;

        public NavItem(String path, String title) {
            this.path = path;
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public String getTitle() {
            return title;
        }
    }
}
