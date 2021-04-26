package com.peregrine.pagerender.server.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

import java.util.List;

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.pagerender.server.models.PageRenderServerConstants.PR_SERVER_COMPONENT_NAV_TYPE;

@Model(
        adaptables = Resource.class,
        resourceType = PR_SERVER_COMPONENT_NAV_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class)
@Exporter(
        name = JACKSON,
        extensions = JSON)
public class NavModel extends AbstractComponent {

    @Inject
    @Default(values = "")
    private String link;

    @Inject
    @Default(values = "")
    private String logo;

    @Inject
    @Default(values ="50")
    private String logosize;

    @Inject
    @Default(values = "")
    private String logoalttext;

    @Inject
    @Default(values = "")
    private String headertext;

    @Inject
    private List<IComponent> links;

    public NavModel (Resource resource){
        super(resource);
    }

    public String getLink() {
        return link;
    }

    public String getLogo() {
        return logo;
    }

    public String getLogosize() {
        return logosize;
    }

    public String getLogoalttext() {
        return logoalttext;
    }

    public String getHeadertext() {
        return headertext;
    }

    public List<IComponent> getLinks() {
        return links;
    }
}
