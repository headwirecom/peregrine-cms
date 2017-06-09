package com.peregrine.admin.data;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Map;

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
