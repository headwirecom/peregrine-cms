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
                System.out.println(parentPage.getResourceType());
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
            Resource parentContent = getParentContent(getResource());
            while(parentContent != null) {
                ValueMap props = ResourceUtil.getValueMap(parentContent);
                Object value = props.get("siteCSS");
                if(value != null) {
                    return (String[]) value;
                }
                parentContent = getParentContent(parentContent);
            }
        }
        return siteCSS;
    }

    public String[] getSiteJS() {
        if(siteJS == null) {
            Resource parentContent = getParentContent(getResource());
            while(parentContent != null) {
                ValueMap props = ResourceUtil.getValueMap(parentContent);
                Object value = props.get("siteJS");
                if(value != null) {
                    return (String[]) value;
                }
                parentContent = getParentContent(parentContent);
            }
        }
        return siteJS;
    }

    public String getTitle() {
        return title;
    }
}