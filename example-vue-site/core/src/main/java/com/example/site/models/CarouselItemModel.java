package com.example.site.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

/**
 * Created by rr on 4/18/2017.
 */
@Model(adaptables = Resource.class, resourceType = "example/components/carouselItem", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class CarouselItemModel extends AbstractComponent {

    @Inject
    private String name;

    @Inject
    private String alt;

    @Inject
    private String heading;

    @Inject
    private String text;

    @Inject @Default(values = "")
    private String imagePath;

    public CarouselItemModel(Resource resource) {
        super(resource);
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getAlt() {
        return alt;
    }

    public String getHeading() {
        return heading;
    }

    public String getText() {
        return text;
    }
}
