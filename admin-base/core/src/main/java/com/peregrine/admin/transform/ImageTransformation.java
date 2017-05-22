package com.peregrine.admin.transform;

/**
 * Created by schaefa on 5/19/17.
 */
public interface ImageTransformation {

    public String getTransformationName();

    public void transform(ImageContext imageContext, OperationContext operationContext)
        throws TransformationException;

    public class TransformationException
        extends Exception
    {
        public TransformationException(String message) {
            super(message);
        }

        public TransformationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public class DisabledTransformationException
        extends TransformationException
    {
        public DisabledTransformationException(String transformationName) {
            super("Service: '" + transformationName + "' is disabled");
        }
    }
}
