package com.peregrine.nodetypes.merge;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by rr on 5/8/2017.
 */
@SuppressWarnings("serial")
public class PageMerge implements Use {

    private final Logger log = LoggerFactory.getLogger(PageMerge.class);

//    @Reference
    ModelFactory modelFactory;

    private SlingHttpServletRequest request;

    public String getMerged() {
        try {
            Map page = modelFactory.exportModelForResource(request.getResource().getChild("jcr:content"),
                    "jackson", Map.class,
                    Collections.<String, String> emptyMap());
            String templatePath = (String) page.get("template");
            if(templatePath != null) {
                Map template = modelFactory.exportModelForResource(request.getResourceResolver().getResource(templatePath).getChild("jcr:content"),
                        "jackson", Map.class,
                        Collections.<String, String> emptyMap());
                return toJSON(merge(template, page));
            }
            return toJSON(page);
        } catch (ExportException e) {
            log.error("not able to export model", e);
        } catch (MissingExporterException e) {
            log.error("not able to find exporter for model", e);
        }
        return "{}";
    }

    private Map merge(Map template, Map page) {
        TreeMap res = new TreeMap();
        res.putAll(template);

        for (Object key: page.keySet()) {
            Object value = page.get(key);
            log.debug("key is {}", key);
            log.debug("value is {}", value.getClass());
            if(value instanceof Map) {

            } else if(value instanceof ArrayList) {
                mergeArrays((ArrayList) res.get(key), (ArrayList) value);
            } else {
                res.put(key, value);
            }
        }
//        res.putAll(page);
        return res;
    }

    private void mergeArrays(ArrayList target, ArrayList value) {
        for (Iterator it = value.iterator(); it.hasNext(); ) {
            Object val = it.next();
            if(!target.contains(val)) {
                target.add(val);
            }
        }
    }

    private String toJSON(Map template) {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer, template);
            writer.close();
        } catch (IOException e) {
            log.error("not able to create string writer", e);
        }
        return writer.toString();
    }

    @Override
    public void init(Bindings bindings) {
        request = (SlingHttpServletRequest) bindings.get("request");
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get("sling");
        modelFactory = sling.getService(ModelFactory.class);
    }
}
