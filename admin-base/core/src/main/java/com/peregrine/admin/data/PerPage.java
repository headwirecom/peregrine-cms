package com.peregrine.admin.data;

/**
 * Peregrine Page with access to their data and parent / child pages.
 * It is adaptable to a Resource and Page Manager.
 *
 * Created by schaefa on 6/2/17.
 */
public interface PerPage
    extends PerBase
//    extends Adaptable
{
    public PerPageManager getPageManager();
    public String getTitle();
    public Iterable<PerPage> listChildren();
    public Iterable<PerPage> listChildren(Filter<PerPage> filter);
    public Iterable<PerPage> listChildren(Filter<PerPage> filter, boolean deep);
    public boolean hasChild(String name);
    public PerPage getParent();
    //AS TODO: The Data Structure for a Template is the same as for a Page. Do we then return a PerPage here instead?
    public PerTemplate getTemplate();
}
