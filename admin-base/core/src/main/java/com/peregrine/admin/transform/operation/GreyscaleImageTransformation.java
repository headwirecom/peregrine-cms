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
        Constants.SERVICE_DESCRIPTION + "=Peregrine: Greyscale Image Transformation (transformation name: vips:greyscale",
        Constants.SERVICE_VENDOR + "=headwire.com, Inc"
    }
)
@Designate(
    ocd = GreyscaleImageTransformation.Configuration.class
)
public class GreyscaleImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String THUMBNAIL_TRANSFORMATION_NAME = "vips:greyscale";

    @ObjectClassDefinition(
        name = "Peregrine: Greyscale Image Transformation Configuration",
        description = "Service to provide Greyscale Image Transformation (requires LIBVIPS to be installed locally otherwise disable this service) for JPEG / PNG files only"
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
    }

    private boolean enabled = false;
    private String transformationName = THUMBNAIL_TRANSFORMATION_NAME;

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
            if(
                !"png".equals(imageContext.getImageType()) &&
                !"jpg".equals(imageContext.getImageType()) &&
                !"jpeg".equals(imageContext.getImageType())
            ) {
                throw new UnsupportedFormatException(imageContext.getImageType());
            }
            // A PNG image cannot be saved directly as PNG with VIPS
            // For that we need to store it as JPEG and then save it as PNG while stripping color info
            boolean requiresConversion = "png".equals(imageContext.getImageType());
            if(requiresConversion) {
                imageContext.setOutputImageType("v");
            }
            transform0(
                imageContext,
                "colourspace",
                // {in}, {out} mark the placement of the input / output file (path / name)
                "{in}", "{out}",
                // Last Parameter is the color space type: Grey 16
                "grey16"
            );
            if(requiresConversion) {
                imageContext.setOutputImageType("png");
                transform0(imageContext, "pngsave",
                    // {in}, {out} mark the placement of the input / output file (path / name)
                    "{in}", "{out}",
                    // Last Parameter is to strip the color settings from JPEG to be able to save it as PNG
                    "--strip=true");
            }
        } else {
            throw new DisabledTransformationException(transformationName);
        }
    }
}
