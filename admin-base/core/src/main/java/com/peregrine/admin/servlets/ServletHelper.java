package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rr on 5/2/2017.
 */
public class ServletHelper {

    public static Map<String,String> convertSuffixToParams(SlingHttpServletRequest request) {

        String suffix = request.getRequestPathInfo().getSuffix();
        HashMap<String, String> ret = new HashMap<String, String>();

        if(suffix != null && suffix.length() > 1) {
            suffix = suffix.substring(1);
            String[] params = suffix.split("//");
            for(int i = 0; i < params.length; i+=2) {
                ret.put(params[i], params[i+1]);
            }
        }

        return ret;

    }

}
