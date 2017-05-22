package com.peregrine.admin.transform;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Created by schaefa on 5/19/17.
 */
public class ImageContext {

    private String imageType = "png";
    private InputStream imageStream;

    public ImageContext(String imageType, InputStream imageStream)
    {
        if(imageType == null || imageType.isEmpty()) {
            throw new IllegalArgumentException("Image Type must be provided (extension without the dot)");
        }
        this.imageType = imageType;
        resetImageStream(imageStream);
    }

    public String getImageType() {
        return imageType;
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
    }
}
