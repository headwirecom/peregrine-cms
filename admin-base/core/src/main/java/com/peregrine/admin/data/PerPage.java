package com.peregrine.admin.data;

/**
 * Peregrine Page with access to their data and parent / child pages.
 * It is adaptable to a Resource and Page Manager.
 *
 * Created by schaefa on 6/2/17.
 */
public interface PerPage
    extends PerBase
{
    /** @return The Page Manager **/
    public PerPageManager getPageManager();
    /** @return Title of the Page if found otherwise null **/
    public String getTitle();
    /** @return List all children of type page **/
    public Iterable<PerPage> listChildren();
    /** @return List all children of type page that passes the filter **/
    public Iterable<PerPage> listChildren(Filter<PerPage> filter);
    /** @return List all children or their children of type page that passes the filter **/
    public Iterable<PerPage> listChildren(Filter<PerPage> filter, boolean deep);
    /** @return If there is a child page with the given name **/
    public boolean hasChild(String name);
    /** @return Parent Page of this page if it is a page otherwise null **/
    public PerPage getParent();
    /** @return Template Page of this page if there is a template and is a page otherwise null **/
    public PerPage getTemplate();
}
