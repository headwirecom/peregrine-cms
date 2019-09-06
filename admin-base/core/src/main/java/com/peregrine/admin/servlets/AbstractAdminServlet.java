package com.peregrine.admin.servlets;

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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.models.factory.ModelFactory;

import java.io.IOException;

/**
 * Basic Admin Servlet for Creation and Deletions
 *
 * To reference both Model Factory and Admin Resource Handle a reference must
 * be added to the @Component definition:
 * <pre>{@code
 * @Component(
 *     ...
 *     reference = {
 *         @Reference(name = "ModelFactory", bind = "setModelFactory", service = ModelFactory.class),
 *         @Reference(name = "AdminResourceHandler", bind = "setResourceManagement", service = AdminResourceHandler.class)
 *     }
 * }</pre>
 */
@SuppressWarnings("serial")
public abstract class AbstractAdminServlet extends AbstractBaseServlet {

    transient ModelFactory modelFactory;

    transient AdminResourceHandler resourceManagement;

    public void setModelFactory(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    public void setResourceManagement(AdminResourceHandler resourceManagement) {
        this.resourceManagement = resourceManagement;
    }

    protected abstract String getType();
    protected abstract String getStatus();
    protected abstract String getFailureMessage();

    protected void enhanceResponse(JsonResponse response, Request request) throws IOException {
        // This is a no-op default implementation
    }
}

