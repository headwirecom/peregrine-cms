package com.example.site.models;

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

/**
 * Created by rr on 4/18/2017.
 */

//[GEN
@Model(
        adaptables = Resource.class,
        resourceType = "example/components/carousel",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)
//GEN]
public class CarouselModel extends Container {

    //[GEN

    public CarouselModel(Resource resource) {
        super(resource);
    }

    //GEN]
}
