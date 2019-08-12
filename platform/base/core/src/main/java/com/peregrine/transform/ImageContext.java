package com.peregrine.transform;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

import static com.peregrine.commons.util.PerConstants.PNG_MIME_TYPE;

/**
 * Context of an Image to be Transformed
 *
 * Created by Andreas Schaefer on 5/19/17.
 */
public class ImageContext {

    /** Mime Type of the Source. PNG is the default **/
    private String sourceMimeType = PNG_MIME_TYPE;
    /** Mime Type of the Target (the same as source by default) **/
    private String targetMimeType = sourceMimeType;
    /** Image Input Stream **/
    private InputStream imageStream;
    /** Indicates if an Image Transformation was not executed and hence the rendition should not be stored **/
    private boolean preventStorage = false;

    /**
     * Image Context where source and target mime type of the same
     * @param sourceMimeType Source and Target Mime Type
     * @param imageStream Image Input Stream
     */
    public ImageContext(String sourceMimeType, InputStream imageStream) {
        this(sourceMimeType, sourceMimeType, imageStream);
    }

    /**
     * Image Context
     * @param sourceMimeType Source Mime Type which must be defined
     * @param targetMimeType Target Mime Type which must be defined
     * @param imageStream  Image Input Stream which cannot be null
     * @throws IllegalArgumentException If source or target mime type or the Input Stream is not defined
     */
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

    /** @return Source Mime Type which is not null and not empty **/
    public String getSourceMimeType() {
        return sourceMimeType;
    }

    /** @return Target Mime Type which is not null and not empty **/
    public String getTargetMimeType() {
        return targetMimeType;
    }

    /**
     * Updates the Target MIME type.
     * @param targetMimeType MIME type to set
     * @return An ImageContext with the specified MIME type
     */
    public ImageContext setTargetMimeType(String targetMimeType) {
        this.targetMimeType = targetMimeType;
        return this;
    }

    /**
     * @return the Image Input Stream which is not null
     */
    public InputStream getImageStream() {
        return imageStream;
    }

    /**
     * Marks an Image Transformation as flawed and so the rendition is not stored
     */
    public void markAsFlawed() {
        preventStorage = true;
    }

    public boolean canBeStored() {
        return !preventStorage;
    }

    /**
     * Resets the Image Stream with a new one. This is only indeted to
     * be used when chaining Image Transformations.
     *
     * This is intended to be used to chain the Image transformation
     * and so this will set the source mime type to the target
     * mime type as it is now in the correct format
     *
     * @param newImageStream New Input Stream to be used
     */
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
