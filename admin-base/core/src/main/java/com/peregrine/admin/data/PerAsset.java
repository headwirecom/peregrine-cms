package com.peregrine.admin.data;

import org.apache.sling.api.resource.Resource;

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
    public InputStream getRenditionStream(String name);
    public InputStream getRenditionStream(Resource resource);
    public Iterable<Resource> listRenditions();
}
