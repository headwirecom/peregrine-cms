package com.peregrine.transform.operation;

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

import com.peregrine.transform.ImageContext;
import com.peregrine.transform.ImageTransformation;
import com.peregrine.transform.OperationContext;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import static com.peregrine.commons.util.PerConstants.PNG_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Grey Scale Image Transformation
 *
 * It is will take an image and make it a greyscale (black and white) image
 *
 * ATTENTION: for now only JPEG and PNG images are supported
 */
@Component(
    service = ImageTransformation.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Greyscale Image Transformation (transformation name: vips:greyscale)",
        SERVICE_VENDOR + EQUAL + PER_VENDOR
    }
)
@Designate(
    ocd = GreyscaleImageTransformation.Configuration.class
)
public class GreyscaleImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String THUMBNAIL_TRANSFORMATION_NAME = "vips:greyscale";
    public static final String OPERATION_NAME_PNGSAVE = "pngsave";
    public static final String GREY_16 = "grey16";
    public static final String STRIP_TRUE = "--strip=true";
    public static final String OPERATION_NAME = "colourspace";

    @ObjectClassDefinition(
        name = "Peregrine: Greyscale Image Transformation Configuration",
        description = "Service to provide Greyscale Image Transformation for JPEG / PNG files only. "
            + "This service does not support any parameters and if provided are ignored"
    )
    public @interface Configuration {

        @AttributeDefinition(
            name = "Enabled",
            description = "Flag to enabled / disabled that service",
            required = true
        )
        boolean enabled() default true;

        @AttributeDefinition(
            name = "Name",
            description = "Transformation Name used to find it in the Rendition Configuration",
            required = true
        )
        String transformationName() default THUMBNAIL_TRANSFORMATION_NAME;
    }

    @Reference
    MimeTypeService mimeTypeService;

    MimeTypeService getMimeTypeService() {
        return mimeTypeService;
    }

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
        configure(configuration.enabled(), configuration.transformationName());
    }

    @Override
    public String getDefaultTransformationName() {
        return THUMBNAIL_TRANSFORMATION_NAME;
    }

    @Override
    public void transform(ImageContext imageContext, OperationContext operationContext)
        throws TransformationException
    {
        if(
            !PNG_MIME_TYPE.equals(imageContext.getSourceMimeType()) &&
            !"image/jpeg".equals(imageContext.getSourceMimeType())
        ) {
            throw new UnsupportedFormatException(imageContext.getSourceMimeType());
        }
        // A PNG image cannot be saved directly as PNG with VIPS
        // For that we need to store it as JPEG and then save it as PNG while stripping color info
        boolean requiresConversion = PNG_MIME_TYPE.equals(imageContext.getSourceMimeType());
        if(requiresConversion) {
            imageContext.setTargetMimeType("v");
        }
        transform0(
            imageContext, VIPS, OPERATION_NAME,
            // {in}, {out} mark the placement of the input / output file (path / name)
            IN_TOKEN, OUT_TOKEN,
            // Last Parameter is the color space type: Grey 16
            GREY_16
        );
        if(requiresConversion) {
            imageContext.setTargetMimeType(PNG_MIME_TYPE);
            transform0(imageContext, VIPS, OPERATION_NAME_PNGSAVE,
                // {in}, {out} mark the placement of the input / output file (path / name)
                IN_TOKEN, OUT_TOKEN,
                // Last Parameter is to strip the color settings from JPEG to be able to save it as PNG
                STRIP_TRUE);
        }
    }
}
