package apps.pagerender.vue.structure.page;

/*-
 * #%L
 * peregrine vuejs page renderer - UI Apps
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import com.peregrine.pagerender.vue.models.PageModel;
import javax.script.Bindings;
import java.util.Map;

import org.apache.sling.scripting.sightly.pojo.Use;
import org.apache.sling.api.resource.Resource;

import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;


public class Helper implements Use {

    private Object model;
    private String siteRootPath;
    private String siteName;
    private String siteLanguage;

    public String getSiteLanguage() {
      return siteLanguage;
    }

    public String getHello() {
        return "hello";
    }

    public String getSiteName() {
        return siteName;
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

    public void init(Bindings bindings) {
        Resource resource = (Resource) bindings.get("resource");
        SlingHttpServletRequest request = (SlingHttpServletRequest) bindings.get("request");
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get("sling");
        Map<String,String> properties = (Map<String,String>) bindings.get("properties");


        try {
          model = sling.getService(ModelFactory.class).getModelFromResource(resource);
        } catch(Throwable t) {
          model = sling.getService(ModelFactory.class).getModelFromRequest(request);
        }

        String path = resource.getPath();
        String lang = "";
        if(path.startsWith("/content/sites/")) {
            path = path.substring("/content/sites/".length());
            lang = (String) ((PageModel) model).getTemplateSiteLanguage();
        } else if(path.startsWith("/content/templates/")) {
            path = path.substring("/content/templates/".length());
            lang = properties.get("siteLanguage");
        }
        if( lang.equals("") ){
          siteLanguage = "en";
        } else {
          siteLanguage = lang;
        }
        int slash = path.indexOf("/");
        siteName = slash > 0 ? path.substring(0, path.indexOf("/")) : path;
        siteRootPath = "/content/sites/"+siteName;
    }
}
