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
@Model(adaptables = Resource.class, resourceType = "example/components/image", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class ImageModel extends AbstractComponent {

    @Inject
    private String title;

    @Inject
    private String caption;

    @Inject @Default(values = "")
    private String imagePath;

    public ImageModel(Resource resource) {
        super(resource);
    }

    public String getTitle() {
        return title;
    }

    public String getCaption() {
        return caption;
    }

    public String getImagePath() {
        return imagePath;
    }

}
