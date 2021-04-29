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
import static com.peregrine.pagerender.server.models.PageRenderServerConstants.PR_SERVER_COMPONENT_PROFILE;


@Model(
        adaptables = Resource.class,
        resourceType = PR_SERVER_COMPONENT_PROFILE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class)
@Exporter(
        name = JACKSON,
        extensions = JSON)
public class ProfileModel extends AbstractComponent {

    @Inject
    @Default(values = "")
    private String firstName;

    @Inject
    @Default(values = "")
    private String lastName;

    @Inject
    @Default(values ="")
    private String tildaPageUri;

    @Inject
    @Default(values = "")
    private String org;

    @Inject
    @Default(values = "")
    private String title;

    @Inject
    @Default(values = "")
    private String pronouns;

    public ProfileModel(Resource resource){
        super(resource);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTildaPageUri() {
        return "/~"+tildaPageUri+".html";
    }

    public String getOrganization() {
        return org;
    }

    public String getPositionTitle() {
        return title;
    }

    public String getPronouns() {
        return pronouns;
    }
}
