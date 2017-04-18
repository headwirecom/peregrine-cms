package com.peregrine.pagerender.vue.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import com.peregrine.nodetypes.models.IComponent;
import com.peregrine.nodetypes.models.AbstractComponent;

/**
 * Created by rr on 12/2/2016.
 */
@Model(adaptables = Resource.class, resourceType = {
        "pagerender/vue/structure/container"
}, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class Container extends AbstractComponent {

    public Container(Resource r) {
        super(r);
    }

    @Inject
    @Named(".")
    private List<IComponent> children;

    @Override
    @JsonIgnore(value = false)
    public List<IComponent> getChildren() {
        return children;
    }

}