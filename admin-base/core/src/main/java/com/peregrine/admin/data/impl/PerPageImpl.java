package com.peregrine.admin.data.impl;

import com.peregrine.admin.data.Filter;
import com.peregrine.admin.data.PerPage;
import com.peregrine.admin.data.PerPageManager;
import com.peregrine.admin.data.PerTemplate;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.util.JcrUtil.JCR_TITLE;

/**
 * Created by schaefa on 6/4/17.
 */
public class PerPageImpl
    extends PerBaseImpl
    implements PerPage
{
    private static final AllFilter allFilter = new AllFilter();

    private PerPageManager pageManager;

    public PerPageImpl(Resource resource) {
        super(resource);
        this.pageManager = new PerPageManagerImpl(resource);
    }

    @Override
    public PerPageManager getPageManager() {
        return pageManager;
    }

    @Override
    public String getTitle() {
        return getContentProperty(JCR_TITLE, String.class);
    }

    @Override
    public Iterable<PerPage> listChildren() {
        return getChildren(allFilter, false);
    }

    @Override
    public Iterable<PerPage> listChildren(Filter<PerPage> filter) {
        return getChildren(filter, false);
    }

    @Override
    public Iterable<PerPage> listChildren(Filter<PerPage> filter, boolean deep) {
        return getChildren(filter, deep);
    }

    private List<PerPage> getChildren(Filter<PerPage> filter, boolean deep) {
        List<PerPage> children = new ArrayList<PerPage>();
        for(Resource child: getResource().getChildren()) {
            PerPage page = child.adaptTo(PerPage.class);
            if(page != null) {
                if(filter.include(page)) {
                    children.add(page);
                    if(deep) {
                        children.addAll(getChildren(filter, deep));
                    }
                }
            }
        }
        return children;
    }

    @Override
    public boolean hasChild(String name) {
        Resource child = getResource().getChild(name);
        return child != null && child.adaptTo(PerPage.class) != null;
    }

    @Override
    public PerPage getParent() {
        Resource parent = getResource().getParent();
        return parent != null ?
            parent.adaptTo(PerPage.class) :
            null;
    }

    @Override
    public PerTemplate getTemplate() {
        return null;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(type.equals(PerPageManager.class)) {
            return (AdapterType) pageManager;
        } else {
            return super.adaptTo(type);
        }
    }

    private static class AllFilter
        implements Filter<PerPage>
    {
        @Override
        public <T> boolean include(T t) {
            return true;
        }
    }
}
