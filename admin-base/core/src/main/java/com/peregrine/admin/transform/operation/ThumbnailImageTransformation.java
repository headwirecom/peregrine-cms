package com.peregrine.admin.transform.operation;

import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import com.peregrine.admin.transform.OperationContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(
    service = ImageTransformation.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Peregrine: Thumbnail Image Transformation (transformation name: vips:thumbnail",
        Constants.SERVICE_VENDOR + "=headwire.com, Inc"
    }
)
@Designate(
    ocd = ThumbnailImageTransformation.Configuration.class
)
public class ThumbnailImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String THUMBNAIL_TRANSFORMATION_NAME = "vips:thumbnail";
    public static final int THUMBNAIL_DEFAULT_WIDTH = 50;
    public static final int THUMBNAIL_DEFAULT_HEIGHT = 50;

    @ObjectClassDefinition(
        name = "Peregrine: Thumbnail Image Transformation Configuration",
        description = "Service to provide Thumbnail Image Transformation (requires LIBVIPS to be installed locally otherwise disable this service)"
    )
    public @interface Configuration {

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

    private boolean enabled = false;
    private String transformationName = THUMBNAIL_TRANSFORMATION_NAME;
    private int defaultWidth = THUMBNAIL_DEFAULT_WIDTH;
    private int getDefaultHeight = THUMBNAIL_DEFAULT_HEIGHT;

    @Activate
    private void activate(final Configuration configuration) {
        configure(configuration);
    }

    @Modified
    private void modified(final Configuration configuration) {
        configure(configuration);
    }

    @Deactivate
    protected void deactivate() {
    }

    private void configure(final Configuration configuration) {
        enabled = configuration.enabled();
        transformationName = configuration.transformationName();
        if(enabled) {
            if(transformationName.isEmpty()) {
                throw new IllegalArgumentException("Transformation Name cannot be empty");
            }
            defaultWidth = configuration.defaultWidth();
            getDefaultHeight = configuration.defaultHeight();
        }
    }

    @Override
    public String getTransformationName() {
        return transformationName;
    }

    @Override
    public void transform(ImageContext imageContext, OperationContext operationContext)
        throws TransformationException
    {
        if(enabled) {
            boolean noCrop = !"false".equals(operationContext.getParameter("noCrop", "false"));
            if(noCrop) {
                transform0(imageContext, "thumbnail",
                    // {in}, {out} mark the placement of the input / output file (path / name)
                    "{in}", "{out}",
                    // Third parameter is width with no tag
                    operationContext.getParameter("width", defaultWidth + ""),
                    // Optional Parameters, double dashes without equals
                    "--height", operationContext.getParameter("height", getDefaultHeight + "")
                );
            } else {
                transform0(imageContext, "thumbnail",
                    // {in}, {out} mark the placement of the input / output file (path / name)
                    "{in}", "{out}",
                    // Third parameter is width with no tag
                    operationContext.getParameter("width", defaultWidth + ""),
                    // Optional Parameters, double dashes without equals
                    "--height", operationContext.getParameter("height", getDefaultHeight + ""),
                    // We crop it a= the center to make it fit within the given width and height
                    "--crop", "centre"
                );
            }
        } else {
            throw new DisabledTransformationException(transformationName);
        }
    }
}
