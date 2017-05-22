package com.peregrine.admin.servlets;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Created by schaefa on 5/22/17.
 */
@ObjectClassDefinition(
    name = "Peregrine: Renditions Configuration",
    description = "Service to provide Image Renditions if image transformation services are found and enabled"
)
public @interface RenditionsConfiguration {
    @AttributeDefinition(
        name = "Renditions",
        description = "Rendition Configuration in the format '<name with extension>|transformation=<transformation name>|<property key>=<property value>|..."
    )
    String[] renditions() default {
        "thumbnail.png|transformation=vips:thumbnail|width=100|height=100",
        "small.png|transformation=vips:thumbnail|width=200|height=200"
    };
}
