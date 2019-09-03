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

import org.jetbrains.annotations.NotNull;

/**
 * Defines the API for Image Transformations
 *
 * ATTENTION: It is important that all Image Transformation do provide
 * their own, unique Transformation Name otherwise they will shadow
 * each other leading to unexpected and undefined behavior.
 * It is probably best to prefix Image Transformation with a project
 * indicator like we used in VIPS where all IT start with 'vips:'.
 *
 * Created by Andreas Schaefer on 5/19/17.
 */
public interface ImageTransformation {

    public static final String SERVICE_DISABLED = "Service: '%s' is disabled";
    public static final String FILE_FORMAT_NOT_SUPPORTED = "File Format: '%s' is not supported for this operation";

    /** @return The Transformation Name **/
    public String getTransformationName();

    /** @return The Default Transformation Name **/
    public @NotNull String getDefaultTransformationName();

    /** @return True if the Transformation is enabled **/
    public boolean isEnabled();

    /**
     * Transforms a given Image with the given Operation
     *
     * @param imageContext Context of the Image to be transformed
     * @param operationContext Context of the Operation used to transform the Image
     * @throws TransformationException If the transformation fails
     */
    public void transform(ImageContext imageContext, OperationContext operationContext)
        throws TransformationException;

    /** Common Transformation Exception **/
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

    /** Transformation Exception if the Operation was disabled **/
    public class DisabledTransformationException
        extends TransformationException
    {
        public DisabledTransformationException(String transformationName) {
            super(String.format(SERVICE_DISABLED, transformationName));
        }
    }

    /** Transformation Exception if the desired output is unsupported **/
    public class UnsupportedFormatException
        extends TransformationException
    {
        public UnsupportedFormatException(String formatName) {
            super(String.format(FILE_FORMAT_NOT_SUPPORTED, formatName));
        }
    }
}
