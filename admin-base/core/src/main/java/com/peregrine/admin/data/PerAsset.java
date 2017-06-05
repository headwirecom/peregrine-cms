package com.peregrine.admin.data;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import javax.jcr.RepositoryException;
import java.io.InputStream;

/**
 * Peregrine Asset with access to their data and renditions.
 * It is adaptable to a Resource and Page Manager.
 *
 * Created by schaefa on 6/4/17.
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
     *
     * @param resource
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
     * @throws PersistenceException
     * @throws RepositoryException
     */
    public void addRendition(String renditionName, InputStream dataStream, String mimeType) throws PersistenceException, RepositoryException;
}
