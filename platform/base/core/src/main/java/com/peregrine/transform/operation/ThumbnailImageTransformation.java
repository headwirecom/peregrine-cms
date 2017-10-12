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

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Creates a Thumbnail Image with the define thumbnail size
 *
 * By default VIPS crops the image if the ratio of the thumbnail size
 * does not match the ones from the source. With no-crop that can be
 * prevented but then the width or height is smaller
 */
@Component(
    service = ImageTransformation.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX +  "Thumbnail Image Transformation (transformation name: vips:thumbnail",
        SERVICE_VENDOR + EQUALS + PER_VENDOR
    }
)
@Designate(
    ocd = ThumbnailImageTransformation.Configuration.class
)
public class ThumbnailImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String THUMBNAIL_TRANSFORMATION_NAME = "vips:thumbnail";
    public static final int THUMBNAIL_DEFAULT_WIDTH = 50;
    public static final int THUMBNAIL_DEFAULT_HEIGHT = 50;

    public static final String NO_CROP = "noCrop";
    public static final String THUMBNAIL = "thumbnail";
    public static final String WIDTH = "width";
    public static final String HEIGHT_PARAMETER = "--height";
    public static final String HEIGHT = "height";
    public static final String CROP_PARAMETER = "--crop";
    public static final String CENTRE = "centre";

    @ObjectClassDefinition(
        name = "Peregrine: Thumbnail Image Transformation Configuration",
        description = "Service to provide Thumbnail Image Transformation (requires LIBVIPS to be installed locally otherwise disable this service)"
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

        @AttributeDefinition(
            name = "Default Width",
            description = "Default width of the Thumbnail if no value is given",
            min = "1"
        )
        int defaultWidth() default THUMBNAIL_DEFAULT_WIDTH;

        @AttributeDefinition(
            name = "Default Height",
            description = "Default height of the Thumbnail if no value is given",
            min = "1"
        )
        int defaultHeight() default THUMBNAIL_DEFAULT_HEIGHT;
    }

    private String transformationName = THUMBNAIL_TRANSFORMATION_NAME;
    private int defaultWidth = THUMBNAIL_DEFAULT_WIDTH;
    private int getDefaultHeight = THUMBNAIL_DEFAULT_HEIGHT;

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
        enabled = configuration.enabled();
        transformationName = configuration.transformationName();
        if(enabled) {
            if(transformationName.isEmpty()) {
                throw new IllegalArgumentException(TRANSFORMATION_NAME_CANNOT_BE_EMPTY);
            }
            if(!checkVips()) {
                throw new IllegalArgumentException(VIPS_IS_NOT_INSTALLED_OR_ACCESSIBLE);
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
            boolean noCrop = !Boolean.FALSE.toString().equals(operationContext.getParameter(NO_CROP, Boolean.FALSE.toString()));
            if(noCrop) {
                transform0(imageContext, THUMBNAIL,
                    // {in}, {out} mark the placement of the input / output file (path / name)
                    IN_TOKEN, OUT_TOKEN,
                    // Third parameter is width with no tag
                    operationContext.getParameter(WIDTH, defaultWidth + ""),
                    // Optional Parameters, double dashes without equals
                    HEIGHT_PARAMETER, operationContext.getParameter(HEIGHT, getDefaultHeight + "")
                );
            } else {
                transform0(imageContext, THUMBNAIL,
                    // {in}, {out} mark the placement of the input / output file (path / name)
                    IN_TOKEN, OUT_TOKEN,
                    // Third parameter is width with no tag
                    operationContext.getParameter(WIDTH, defaultWidth + ""),
                    // Optional Parameters, double dashes without equals
                    HEIGHT_PARAMETER, operationContext.getParameter(HEIGHT, getDefaultHeight + ""),
                    // We crop it at the center to make it fit within the given width and height
                    CROP_PARAMETER, CENTRE
                );
            }
        } else {
            throw new DisabledTransformationException(transformationName);
        }
    }
}
