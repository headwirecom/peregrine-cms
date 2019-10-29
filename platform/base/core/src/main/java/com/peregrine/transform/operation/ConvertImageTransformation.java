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
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX +  "Convert Image Transformation (transformation name: vips:convert)",
        SERVICE_VENDOR + EQUAL + PER_VENDOR
    }
)
@Designate(
    ocd = ConvertImageTransformation.Configuration.class
)
public class ConvertImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String DEFAULT_TRANSFORMATION_NAME = "vips:convert";
    public static final String OPERATION_NAME = "copy";

    @ObjectClassDefinition(
        name = "Peregrine: Convert Image Transformation Configuration",
        description = "Service to provide Image Transformation to convert images. "
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
        String transformationName() default DEFAULT_TRANSFORMATION_NAME;
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

    protected void configure(final Configuration configuration) {
        configure(configuration.enabled(), configuration.transformationName());
    }

    @Override
    public String getDefaultTransformationName() {
        return DEFAULT_TRANSFORMATION_NAME;
    }

    @Override
    public void transform(ImageContext imageContext, OperationContext operationContext)
        throws TransformationException
    {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(IN_TOKEN);
        parameters.add(OUT_TOKEN);
        log.trace("Copy Image: name: '{}'", getTransformationName());
        transform0(imageContext, VIPS, OPERATION_NAME, parameters.toArray(new String[] {}));
    }
}
