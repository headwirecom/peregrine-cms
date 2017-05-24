package com.peregrine.admin.transform;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Created by schaefa on 5/19/17.
 */
public class ImageContext {

    private String sourceMimeType = "image/png";
    private String targetMimeType = sourceMimeType;
    private InputStream imageStream;

    public ImageContext(String sourceMimeType, InputStream imageStream) {
        this(sourceMimeType, sourceMimeType, imageStream);
    }

    public ImageContext(String sourceMimeType, String targetMimeType, InputStream imageStream)
    {
        if(sourceMimeType == null || sourceMimeType.isEmpty()) {
            throw new IllegalArgumentException("Source Mime Type must be provided");
        }
        if(targetMimeType == null || targetMimeType.isEmpty()) {
            throw new IllegalArgumentException("Output Image Mime Type must be provided");
        }
        resetImageStream(imageStream);
        // The reset of the image stream in changing the output mime type so we set it later
        this.sourceMimeType = sourceMimeType;
        this.targetMimeType = targetMimeType;
    }

    public String getSourceMimeType() {
        return sourceMimeType;
    }

    public String getTargetMimeType() {
        return targetMimeType;
    }

    public ImageContext setTargetMimeType(String targetMimeType) {
        this.targetMimeType = targetMimeType;
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
        if(!sourceMimeType.equals(targetMimeType)) {
            this.sourceMimeType = targetMimeType;
        }
    }
}
