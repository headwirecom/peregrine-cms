package com.peregrine.pagerender.server.helpers;

import javax.script.Bindings;
import com.peregrine.pagerender.server.models.PageModel;

import java.util.Objects;

public class PageHelper extends BaseHelper {

    public String getPath() {
        return this.getResource().getPath();
    }

    public String getSiteRootPath() {
        return Objects.nonNull(getSiteRoot()) ? getSiteRoot().getPath() : null;
    }

    public String getModelClass() {
        return Objects.nonNull(getModel()) ? getModel().getClass().toString() : null;
    }

    public String[] getSiteCSS(){
        PageModel page = getResource().adaptTo(PageModel.class);
        return page.getSiteCSS();
    }

    public void init(Bindings bindings) {
        super.init(bindings);
    }
}