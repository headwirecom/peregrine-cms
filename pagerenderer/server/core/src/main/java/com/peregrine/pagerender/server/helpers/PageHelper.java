package com.peregrine.pagerender.server.helpers;

import javax.script.Bindings;

import com.peregrine.pagerender.server.models.PageModel;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;

public class PageHelper implements Use {

    private Object model;
    private String siteRootPath;
    private Resource resource;

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
        return siteRootPath;
    }

    public String getModelClass() {
        return model.getClass().toString();
    }

    public String[] getSiteCSS(){
        PageModel page = resource.adaptTo(PageModel.class);
        return page.getSiteCSS();
    }


    public void init(Bindings bindings) {
        resource = (Resource) bindings.get("resource");
        SlingHttpServletRequest request = (SlingHttpServletRequest) bindings.get("request");
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get("sling");

        String path = resource.getPath();
        path = path.substring("/content/".length());

        int slash = path.indexOf("/");
        String siteName = slash > 0 ? path.substring(0, path.indexOf("/")) : path;
        siteRootPath = "/content/" + siteName + "/pages";

        try {
            model = sling.getService(ModelFactory.class).getModelFromResource(resource);
        } catch(Throwable t) {
            model = sling.getService(ModelFactory.class).getModelFromRequest(request);
        }
        model.toString();
    }

}