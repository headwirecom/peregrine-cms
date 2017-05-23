package com.peregrine.admin.transform;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schaefa on 5/22/17.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = ImageTransformationSetup.class
)
@Designate(ocd = ImageTransformationSetup.Configuration.class, factory = true)
public class ImageTransformationSetup {

    @ObjectClassDefinition(
        name = "Peregrine: Image Transformation Setup",
        description = "Each instance provides a detailed configuration of the Image Transformation which can be used to chain Image Transformations"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Name",
            description = "Name of the Setup. Cannot be the same as an Image Transformation Service",
            required = true
        )
        String setupName();

        @AttributeDefinition(
            name = "Configuration",
            description = "A list of image transformation configuration executed in the given order. Format: <image transformation name>=[<property key>=<property value>]*"
        )
        String[] imageTransformationConfigurations();
    }

    private String name;
    private List<ImageTransformationConfiguration> imageTransformationConfigurations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<ImageTransformationConfiguration> getImageTransformationConfigurations() {
        return imageTransformationConfigurations;
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) {
        setup(configuration);
    }

    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) {
        setup(configuration);
    }

    private void setup(Configuration configuration) {
        name = configuration.setupName();
        for(String imageTransformationConfiguration: configuration.imageTransformationConfigurations()) {
            imageTransformationConfigurations.add(new ImageTransformationConfiguration(name, imageTransformationConfiguration));
        }
    }
}
