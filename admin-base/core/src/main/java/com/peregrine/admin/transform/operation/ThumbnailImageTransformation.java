package com.peregrine.admin.transform.operation;

import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import com.peregrine.admin.transform.OperationContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import static com.peregrine.admin.transform.operation.ThumbnailImageConfiguration.THUMBNAIL_DEFAULT_HEIGHT;
import static com.peregrine.admin.transform.operation.ThumbnailImageConfiguration.THUMBNAIL_DEFAULT_WIDTH;
import static com.peregrine.admin.transform.operation.ThumbnailImageConfiguration.THUMBNAIL_TRANSFORMATION_NAME;

/**
 * Created by schaefa on 5/19/17.
 */
@Component(
    service = ImageTransformation.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Peregrine: Thumbnail Image Transformation (transformation name: vips:thumbnail",
        Constants.SERVICE_VENDOR + "=headwire.com, Inc"
    }
)
@Designate(
    ocd = ThumbnailImageConfiguration.class
)
public class ThumbnailImageTransformation
    extends AbstractVipsImageTransformation
{
    private boolean enabled = false;
    private String transformationName = THUMBNAIL_TRANSFORMATION_NAME;
    private int defaultWidth = THUMBNAIL_DEFAULT_WIDTH;
    private int getDefaultHeight = THUMBNAIL_DEFAULT_HEIGHT;

    @Activate
    private void activate(final ThumbnailImageConfiguration configuration) {
        configure(configuration);
    }

    @Modified
    private void modified(final ThumbnailImageConfiguration configuration) {
        configure(configuration);
    }

    @Deactivate
    protected void deactivate() {
    }

    private void configure(final ThumbnailImageConfiguration configuration) {
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
            transform0(imageContext, "thumbnail",
                // {in}, {out} mark the placement of the input / output file (path / name)
                "{in}", "{out}",
                // Third parameter is width with no tag
                operationContext.getParameter("width", defaultWidth + ""),
                // Optional Parameters, double dashes without equals
                "--height", operationContext.getParameter("height", getDefaultHeight + ""),
                // We crop it at the center to make it fit within the given width and height
                "--crop", "centre");
        } else {
            throw new DisabledTransformationException(transformationName);
        }
    }
}
