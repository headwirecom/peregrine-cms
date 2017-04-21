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
@Model(adaptables = Resource.class, resourceType = "example/components/nav", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class NavModel extends AbstractComponent {

    @Inject @Default(values = "")
    private String brand;

    public NavModel(Resource resource) {
        super(resource);
    }

    public String getBrand() {
        return brand == null ? "" : brand;
    }

}
