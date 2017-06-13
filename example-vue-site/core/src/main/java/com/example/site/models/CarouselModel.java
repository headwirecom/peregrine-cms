package com.example.site.models;

import com.peregrine.nodetypes.models.Container;
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

    @Inject @Default(values = "5000")
    private String interval;

    @Inject @Default(values = "hover")
    private String pause;

    @Inject @Default(values = "true")
    private String ride;

    @Inject @Default(values = "true")
    private String indicators;

    @Inject @Default(values = "true")
    private String controls;

    @Inject @Default(values = "true")
    private String wrap;

    @Inject @Default(values = "true")
    private String keyboard;

    public String getInterval() {
        return interval;
    }

    public String getPause() {
        return pause;
    }

    public String getRide() {
        return ride;
    }

    public String getControls() {
        return controls;
    }

    public String getIndicators() {
        return indicators;
    }

    public String getWrap() {
        return wrap;
    }

    public String getKeyboard() {
        return keyboard;
    }


    //GEN]
}
