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
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX +  "Generic Image Transformation",
        SERVICE_VENDOR + EQUAL + PER_VENDOR
    }
)
@Designate(
    ocd = GenericImageTransformation.Configuration.class, factory = true
)
public class GenericImageTransformation
    extends AbstractVipsImageTransformation
{
    public static final String DEFAULT_TRANSFORMATION_NAME = "generic";

    @ObjectClassDefinition(
        name = "Peregrine: Generic Image Transformation Configuration",
        description = "Service to provide a Generic Image Transformation"
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
        String transformationName();

        @AttributeDefinition(
            name = "Command",
            description = "Shell Command for this Transformation",
            required = true
        )
        String command();

        @AttributeDefinition(
            name = "Command Line",
            description = "Each Entry defines a single command parameter. Use {in} and {out} as input/output file placeholders. " +
                "Parameters are enclosed in double curly brackets: {{<parameter name>}}",
            min = "1"
        )
        String[] cli();

        @AttributeDefinition(
            name = "Notes",
            description = "Instructional Notes for this Transformation"
        )
        String notes();
    }

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

    private String command;
    private List<ParameterHandler> parameters;

    protected void configure(final Configuration configuration) {
        configure(configuration.enabled(), configuration.transformationName());
        command = configuration.command();
        List<String> clis = Arrays.asList(configuration.cli());
        parameters = clis.stream()
            .map(p -> new ParameterHandler(p))
            .collect(Collectors.toList());
    }

    @Override
    public void transform(ImageContext imageContext, final OperationContext operationContext)
        throws TransformationException
    {
        List<String> parameterList = new ArrayList<>();
        for(ParameterHandler handler: parameters) {
            parameterList.addAll(handler.resolve(operationContext));
        }
        transform0(imageContext, command, null, parameterList.toArray(new String[] {}));
    }

    static class ParameterHandler {
        private String parameter;
        private String placeholder;
        private boolean oneEntry;
        private String defaultValue = "";

        public ParameterHandler(String parameter) {
            String param = parameter == null ? "" : parameter.trim();
            int start = param.indexOf("{{");
            int end = param.indexOf("}}");
            if(start >= 0 && end > start) {
                String temp = param.substring(start + 2, end);
                int index = temp.indexOf("||");
                if(index >= 0) {
                    if(index < 1) {
                        throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (no parameter before ||)");
                    }
                    if(index > temp.length() - 2) {
                        throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (no placeholder after ||)");
                    }
                    if(start != 0) {
                        throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (split placeholder must not have a prefix)");
                    }
                    if(end < param.length() - 2) {
                        throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (split placeholder must not have a suffix)");
                    }
                    this.parameter = temp.substring(0, index);
                    placeholder = temp.substring(index + 2);
                } else {
                    this.parameter = param;
                    placeholder = temp;
                    oneEntry = true;
                }
            } else if(start >= 0 && end < 0) {
                throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (no end }})");
            } else if(start < 0 && end >= 0) {
                throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (no start {{)");
            } else if(start > end) {
                throw new IllegalArgumentException("Parameter: " + parameter + " is not valid (end }} before start {{)");
            } else {
                this.parameter = param;
            }
        }

        public List<String> resolve(OperationContext operationContext) {
            List<String> answer = new ArrayList<>();
            if(placeholder != null && !placeholder.isEmpty()) {
                if(oneEntry) {
                    String value = operationContext.getParameter(placeholder, defaultValue);
                    if(value != null && !value.isEmpty()) {
                        String temp = parameter.replace("{{" + placeholder + "}}", value);
                        LoggerFactory.getLogger(ParameterHandler.class.getName()).info(
                            "Parameter: {}, Placeholder: {}, Value: {}, result: {}",
                            parameter, placeholder, value, temp
                        );
                        answer.add(temp);
                    }
                } else {
                    String value = operationContext.getParameter(placeholder, defaultValue);
                    if(value != null && !value.isEmpty()) {
                        if (parameter != null && !parameter.isEmpty()) {
                            answer.add(parameter);
                        }
                        answer.add(operationContext.getParameter(placeholder, defaultValue));
                    }
                }
            } else {
                answer.add(parameter);
            }
            return answer;
        }
    }
}
