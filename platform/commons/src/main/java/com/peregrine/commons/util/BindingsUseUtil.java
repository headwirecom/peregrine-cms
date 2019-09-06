package com.peregrine.commons.util;

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

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;

public final class BindingsUseUtil {

	public static final String REQUEST = "request";
	public static final String SLING = "sling";

	private BindingsUseUtil() {
		throw new UnsupportedOperationException();
	}

	public static SlingHttpServletRequest getRequest(final Bindings bindings) {
		return (SlingHttpServletRequest) bindings.get(REQUEST);
	}

	public static SlingScriptHelper getSling(final Bindings bindings) {
		return (SlingScriptHelper) bindings.get(SLING);
	}

}
