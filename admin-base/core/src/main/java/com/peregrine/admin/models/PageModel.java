package com.peregrine.admin.models;

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rr on 12/2/2016.
 */
@Model(adaptables = Resource.class, resourceType = {
        "admin/components/toolingpage"
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

    @Inject @Named("jcr:description")
    private String description;

    @Inject
    private String dataFrom;

    @Inject
    private String dataDefault;

    @Inject
    private String[] loaders;

    @Inject
    private String[] suffixToParameter;

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

    public String getDescription() {
        return description;
    }

    public String getDataFrom() {
        return dataFrom;
    }
    public String getDataDefault() {
        return dataDefault;
    }
    public String[] getLoaders() {
        return loaders;
    }

    public String[] getSuffixToParameter() {
        return suffixToParameter;
    }
}