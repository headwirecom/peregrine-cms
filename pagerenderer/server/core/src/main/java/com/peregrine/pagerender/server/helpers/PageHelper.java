package com.peregrine.pagerender.server.helpers;

import javax.script.Bindings;
import com.peregrine.pagerender.server.models.PageModel;

import java.util.Objects;

public class PageHelper extends BaseHelper {

    public String getHello() {
        return "hello";
    }

    public String getPath() {
        return resource.getPath();
    }

    public Object getModel() {
        return model;
    }

    public String getSiteRootPath() {
        return Objects.nonNull(siteRoot) ? siteRoot.getPath() : null;
    }

    public String getModelClass() {
        return Objects.nonNull(model) ? model.getClass().toString() : null;
    }

    public String[] getSiteCSS(){
        PageModel page = resource.adaptTo(PageModel.class);
        return page.getSiteCSS();
    }

    public void init(Bindings bindings) {
        super.init(bindings);
    }

}