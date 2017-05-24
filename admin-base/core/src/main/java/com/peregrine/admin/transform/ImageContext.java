package com.peregrine.admin.transform;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Created by schaefa on 5/19/17.
 */
public class ImageContext {

    private String imageType = "png";
    private String outputImageType = imageType;
    private InputStream imageStream;

    public ImageContext(String imageType, InputStream imageStream) {
        this(imageType, imageType, imageStream);
    }

    public ImageContext(String imageType, String outputImageType, InputStream imageStream)
    {
        if(imageType == null || imageType.isEmpty()) {
            throw new IllegalArgumentException("Image Type must be provided (extension without the dot)");
        }
        if(outputImageType == null || outputImageType.isEmpty()) {
            throw new IllegalArgumentException("Output Image Type must be provided (extension without the dot)");
        }
        resetImageStream(imageStream);
        this.imageType = imageType;
        this.outputImageType = outputImageType;
    }

    public String getImageType() {
        return imageType;
    }

    public String getOutputImageType() {
        return outputImageType;
    }

    public ImageContext setOutputImageType(String outputImageType) {
        this.outputImageType = outputImageType;
        return this;
    }

    public InputStream getImageStream() {
        return imageStream;
    }

    public void resetImageStream(InputStream newImageStream) {
        if(newImageStream == null) {
            throw new IllegalArgumentException("Image Input Stream cannot be null");
        }
        //AS TODO: Shall we check for EOF?
        if(this.imageStream != null) {
            IOUtils.closeQuietly(this.imageStream);
        }
        this.imageStream = newImageStream;
        if(!imageType.equals(outputImageType)) {
            this.imageType = outputImageType;
        }
    }
}
