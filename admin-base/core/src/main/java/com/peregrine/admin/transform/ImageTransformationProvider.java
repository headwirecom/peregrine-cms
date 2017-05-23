package com.peregrine.admin.transform;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Image Transformation Provider to manage the available
 * Image Transformation by transformation name which normally
 * is also the operation name
 */
@Component(
    service = ImageTransformationProvider.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Peregrine Image Transformation Service Provider",
        Constants.SERVICE_VENDOR + "=headwire.com, Inc"
    }
)
public class ImageTransformationProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, ImageTransformation> imageTransformations = new HashMap<>();

    public ImageTransformation getImageTransformation(String transformationName) {
        return imageTransformations.get(transformationName);
    }

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindImageTransformation(ImageTransformation imageTransformation) {
        String transformationName = imageTransformation.getTransformationName();
        if(transformationName != null && !transformationName.isEmpty()) {
            imageTransformations.put(transformationName, imageTransformation);
        } else {
            log.error("Image Transformation: '{}' does not provide an operation name -> binding is ignored", imageTransformation);
        }
    }

    @SuppressWarnings("unused")
    public void unbindImageTransformation(ImageTransformation imageTransformation) {
        String transformationName = imageTransformation.getTransformationName();
        if(imageTransformations.containsKey(transformationName)) {
            imageTransformations.remove(transformationName);
        } else {
            log.error("Image Transformation: '{}' is not register with operation name: '{}' -> unbinding is ignored", imageTransformation, transformationName);
        }
    }
}
