package com.peregrine.sitemap.impl;

/*-
 * #%L
 * platform base - Core
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

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.Strings.COLON;
import static com.peregrine.commons.Strings.EQ;
import static java.util.Objects.nonNull;

public final class XmlNamespaceUtils {

    public static final String XMLNS = "xmlns";
    private static final String XMLNS_PREFIX = XMLNS + COLON;

    private XmlNamespaceUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean parseMappingAddPrefix(final String mapping, final Map<String, String> target) {
        if (StringUtils.contains(mapping, EQ)) {
            final String key = XMLNS_PREFIX + StringUtils.substringBefore(mapping, EQ);
            final String value = StringUtils.substringAfter(mapping, EQ);
            target.put(key, value);
            return true;
        }

        return false;
    }

    public static Map<String, String> parseMappingsAddPrefix(final String... mappings) {
        final HashMap<String, String> result = new HashMap<>();
        if (nonNull(mappings)) {
            for (final String mapping : mappings) {
                parseMappingAddPrefix(mapping, result);
            }
        }

        return result;
    }

}
