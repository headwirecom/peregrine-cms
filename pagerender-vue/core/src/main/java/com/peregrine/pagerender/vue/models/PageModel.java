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
import org.apache.sling.models.factory.ModelFactory;

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
    private ModelFactory modelFactory;

    @Inject
    private String[] siteCSS;

    @Inject
    private String[] siteJS;

    @Inject @Named("template")
    private String template;

    @Inject @Named("jcr:title")
    private String title;

    public String[] getSiteCSS() {
        if(siteCSS == null) {
            String[] value = (String[]) getInheritedProperty("siteCSS");
            if (value != null && value.length != 0) return value;
            if (getTemplate() != null) {
                PageModel templatePageModel = getTamplatePageModel();
                if(templatePageModel != null) {
                    return templatePageModel.getSiteCSS();
                }
            }
        }
        return siteCSS;
    }

    private PageModel getTamplatePageModel() {
        Resource templateResource = getResource().getResourceResolver().getResource(getTemplate()+"/jcr:content");
        return (PageModel) modelFactory.getModelFromResource(templateResource);
    }

    private Object getInheritedProperty(String propertyName) {
        Resource parentContent = getParentContent(getResource());
        while(parentContent != null) {
            ValueMap props = ResourceUtil.getValueMap(parentContent);
            Object value = props.get(propertyName);
            if(value != null) {
                return value;
            }
            parentContent = getParentContent(parentContent);
        }
        return null;
    }

    public String[] getSiteJS() {
        if(siteJS == null) {
            String[] value = (String[]) getInheritedProperty("siteJS");
            if (value != null && value.length != 0) return value;
            PageModel templatePageModel = getTamplatePageModel();
            if(templatePageModel != null) {
                return templatePageModel.getSiteJS();
            }
        }
        return siteJS;
    }

    public String getTemplate() {
        if(template == null) {
            String value = (String) getInheritedProperty("template");
            if (value != null) {
                this.template = template;
                return value;
            }
        }
        return template;
    }

    public String getTitle() {
        return title;
    }
}