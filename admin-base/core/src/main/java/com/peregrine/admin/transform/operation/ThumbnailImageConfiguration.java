package com.peregrine.admin.transform.operation;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Created by schaefa on 5/22/17.
 */
@ObjectClassDefinition(
    name = "Peregrine: Thumbnail Image Rendition Configuration",
    description = "Service to provide Thumbnail Image Rendition (requires LIBVIPS to be installed locally otherwise disable this service)"
)
public @interface ThumbnailImageConfiguration {
    String THUMBNAIL_TRANSFORMATION_NAME = "vips:thumbnail";
    int THUMBNAIL_DEFAULT_WIDTH = 50;
    int THUMBNAIL_DEFAULT_HEIGHT = 50;

    @AttributeDefinition(
        name = "Enabled",
        description = "Flag to enabled / disabled that service",
        required = true
    )
    boolean enabled() default false;

    @AttributeDefinition(
        name = "Name",
        description = "Transformation Name used to find it in the Rendition Configuration",
        required = true
    )
    String transformationName() default THUMBNAIL_TRANSFORMATION_NAME;

    @AttributeDefinition(
        name = "Default Width",
        description = "Default width of the Thumbnail if no value is given",
        min = "1"
    )
    int defaultWidth() default THUMBNAIL_DEFAULT_WIDTH;

    @AttributeDefinition(
        name = "Default Height",
        description = "Default height of the Thumbnail if no value is given",
        min = "1"
    )
    int defaultHeight() default THUMBNAIL_DEFAULT_HEIGHT;
}
