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

import java.util.ArrayList;

import static com.peregrine.commons.util.PerUtil.EQUAL;
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
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX +  "Thumbnail Image Transformation (transformation name: vips:thumbnail",
        SERVICE_VENDOR + EQUAL + PER_VENDOR
    }
)
@Designate(
    ocd = ThumbnailImageTransformation.Configuration.class
)
public class ThumbnailImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String DEFAULT_TRANSFORMATION_NAME = "vips:thumbnail";
    public static final int DEFAULT_WIDTH = 50;
    public static final int DEFAULT_HEIGHT = 50;

    public static final String NO_CROP = "noCrop";
    public static final String OPERATION_NAME = "thumbnail";
    public static final String WIDTH = "width";
    public static final String HEIGHT_PARAMETER = "--height";
    public static final String HEIGHT = "height";
    public static final String CROP_PARAMETER = "--crop";
    public static final String CENTRE = "centre";

    @ObjectClassDefinition(
        name = "Peregrine: Thumbnail Image Transformation Configuration",
        description = "Service to provide Thumbnail Image Transformation. "
            + "This service requires a greater than 0 width, an optional height (if <= 0 is ignored) and an optional noCrop flag (false/true) which if true is not cropping the image. "
            + "If height is not set the image is scaled down and with noCrop = true the aspect radio is respected otherwise the image becomes a square"
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
        String transformationName() default DEFAULT_TRANSFORMATION_NAME;

        @AttributeDefinition(
            name = "Default Width",
            description = "Default width of the Thumbnail if no value is given",
            min = "1"
        )
        int defaultWidth() default DEFAULT_WIDTH;

        @AttributeDefinition(
            name = "Default Height",
            description = "Default height of the Thumbnail if no value is given",
            min = "1"
        )
        int defaultHeight() default DEFAULT_HEIGHT;
    }

    private int defaultWidth = DEFAULT_WIDTH;
    private int defaultHeight = DEFAULT_HEIGHT;

    @Reference
    MimeTypeService mimeTypeService;

    MimeTypeService getMimeTypeService() {
        return mimeTypeService;
    }

    @Override
    public String getDefaultTransformationName() {
        return DEFAULT_TRANSFORMATION_NAME;
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

    protected void configure(final Configuration configuration) {
        configure(configuration.enabled(), configuration.transformationName());
        defaultWidth = configuration.defaultWidth();
        defaultHeight = configuration.defaultHeight();
        if(defaultWidth <= 0) {
            throw new IllegalArgumentException(TRANSFORMATION_WIDTH_MUST_BE_PROVIDED);
        }
    }

    @Override
    public void transform(ImageContext imageContext, OperationContext operationContext)
        throws TransformationException
    {
        ArrayList<String> parameters = new ArrayList<>();
        boolean noCrop = !Boolean.FALSE.toString().equals(operationContext.getParameter(NO_CROP, Boolean.FALSE.toString()));
        int width = Integer.parseInt(operationContext.getParameter(WIDTH, defaultWidth + ""));
        int height = Integer.parseInt(operationContext.getParameter(HEIGHT, defaultHeight + ""));

        parameters.add(IN_TOKEN);
        parameters.add(OUT_TOKEN);
        parameters.add(width + "");
        if(height > 0) {
            parameters.add(HEIGHT_PARAMETER);
            parameters.add(height + "");
        }
        if(!noCrop) {
            parameters.add(CROP_PARAMETER);
            parameters.add(CENTRE);
        }
        log.trace("Thumbnail Image: name: '{}', height: '{}', width: '{}', no-crop: '{}'", getTransformationName(), height, width, noCrop);
        transform0(imageContext, VIPS, OPERATION_NAME, parameters.toArray(new String[] {}));
    }
}
