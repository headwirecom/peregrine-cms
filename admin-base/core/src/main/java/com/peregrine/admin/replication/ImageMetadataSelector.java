package com.peregrine.admin.replication;

/**
 * Created by schaefa on 6/9/17.
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
