package com.peregrine.pagerender.vue.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by rr on 12/2/2016.
 */
@Model(adaptables = Resource.class, resourceType = {
        "pagerender/vue/structure/page"
}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class PageModel extends Container {

    public PageModel(Resource r) {
        super(r);
    }

    public Resource getParentContent(Resource res) {
        Resource page = res.getParent();
        if(page != null) {
            Resource parentPage = page.getParent();
            if(parentPage != null) {
                if("per:Page".equals(parentPage.getResourceType())) {
                    Resource child =  parentPage.getChild("jcr:content");
                    return child;
                }
            }
        }
        return null;
    }

    @Inject
    private String[] siteCSS;

    @Inject
    private String[] siteJS;

    @Inject @Named("jcr:title")
    private String title;

    public String[] getSiteCSS() {
        if(siteCSS == null) {
            String[] value = getInheritedProperty("siteCSS");
            if (value != null) return value;
        }
        return siteCSS;
    }

    private String[] getInheritedProperty(String propertyName) {
        Resource parentContent = getParentContent(getResource());
        while(parentContent != null) {
            ValueMap props = ResourceUtil.getValueMap(parentContent);
            Object value = props.get(propertyName);
            if(value != null) {
                return (String[]) value;
            }
            parentContent = getParentContent(parentContent);
        }
        return new String[]{};
    }

    public String[] getSiteJS() {
        if(siteJS == null) {
            String[] value = getInheritedProperty("siteJS");
            if (value != null) return value;
        }
        return siteJS;
    }

    public String getTitle() {
        return title;
    }
}