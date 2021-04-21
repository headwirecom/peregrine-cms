package com.peregrine.pagerender.server.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import javax.inject.Inject;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.pagerender.server.models.PageRenderServerConstants.PR_SERVER_COMPONENT_FOOTER_TYPE;

@Model(
        adaptables = Resource.class,
        resourceType = PR_SERVER_COMPONENT_FOOTER_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class)
@Exporter(
        name = JACKSON,
        extensions = JSON)
public class FooterModel extends AbstractComponent {

    @Inject
    @Default(values = "")
    private String text;

    public FooterModel(Resource resource) {
        super(resource);
    }

    public String getText() {
        return text == null ? "" : text;
    }
}
