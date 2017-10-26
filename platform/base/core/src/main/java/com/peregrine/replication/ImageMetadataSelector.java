package com.peregrine.replication;

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

/**
 * Defines what Image Metadata are stored
 *
 * Created by Andreas Schaefer on 6/9/17.
 */
public interface ImageMetadataSelector {

    /** @return True if the properties should be added to a single JCR property as JSon object **/
    public boolean asJsonProperty();

    /**
     * Check if this category accepted by the selector
     * @param category Name of the Category to be checked
     * @return Adjusted Category Name if accepted otherwise null
     */
    public String acceptCategory(String category);

    /**
     * Check if this Tag accepted by the selector
     * @param tag Name of the Tag to be checked
     * @return Adjusted Tag Name if accepted otherwise null
     */
    public String acceptTag(String tag);
}
