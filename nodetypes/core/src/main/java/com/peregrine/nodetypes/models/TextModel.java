package com.peregrine.nodetypes.models;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Model(adaptables = Resource.class, resourceType = "per/components/content/text", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class TextModel extends AbstractComponent {

    @Inject @Default(values = "")
    private String text;

    public TextModel(Resource resource) {
        super(resource);
    }


    public String getText() {
        return text == null ? "" : text;
    }

}