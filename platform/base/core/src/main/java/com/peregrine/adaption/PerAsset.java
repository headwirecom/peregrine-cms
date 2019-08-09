package com.peregrine.adaption;

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

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Map;

/**
 * Peregrine Asset with access to their data and renditions.
 * It is adaptable to a Resource and Page Manager.
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
public interface PerAsset
    extends PerBase
{
    /**
     * Looks for the Rendition with the given name
     * @param name The name of the rendition or null for the original image
     * @return The rendition data stream of the rendition or original image if found.
     *         If the name is not null but not found the return value is null.
     */
    public InputStream getRenditionStream(String name);

    /**
     * Obtain the Input Stream of the Rendition Resource
     *
     * @param resource Resource of the desired rendition. If null then it will use
     *                 the original Asset otherwise it assumes it is a rendition resource
     * @return The rendition of the resource and if null the original image
     */
    public InputStream getRenditionStream(Resource resource);

    /**
     * @return The available renditions
     */
    public Iterable<Resource> listRenditions();

    /**
     * Creates the renditions folder if needed, creates the rendition resource,
     * sets the mime type and add the data
     *
     * @param renditionName Name of the Rendition
     * @param dataStream Input Stream of the rendition image
     * @param mimeType Mime-Type of the Image
     *
     * @throws PersistenceException If there's a persistence error
     * @throws RepositoryException If there's a repository error
     */
    public void addRendition(String renditionName, InputStream dataStream, String mimeType) throws PersistenceException, RepositoryException;

    /**
     * Add a Image Metadata Tag to the Asset
     *
     * @param category Name of the category
     * @param tag Name of the tag
     * @param value Value of the tag
     * @throws PersistenceException If necessary resource could not be created
     * @throws RepositoryException General Access issue to the JCR Tree
     */
    public void addTag(String category, String tag, Object value)
        throws PersistenceException, RepositoryException;

    /** @return All the image tags of this Asset. Map maybe empty but never null **/
    public Map<String, Map<String, Object>> getTags();

    /**
     * @param category Name of the category
     * @return All the image tags of this Asset's Category.
     *         Map may be empty but never null
     **/
    public Map<String, Object> getTags(String category);

    /**
     * Get a specific tag of this asset
     * @param category Name of the Category of the Asset
     * @param tag Name of the Tag
     * @return The value of the tag or null if not found
     */
    public Object getTag(String category, String tag);
}
