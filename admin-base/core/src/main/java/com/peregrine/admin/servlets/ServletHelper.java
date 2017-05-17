package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;

import java.io.*;
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

    public static String componentPathToName(String path) {
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < segments.length; i++) {
            if(i != 2) {
                sb.append("-");
            }
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    public static String componentNameToPath(String name) {
        String[] segments = name.split("-");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < segments.length; i++) {
            if(i != 0) {
                sb.append("/");
            }
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    public static void echo(InputStream input, PrintWriter writer) throws IOException {
        char[] buffer = new char[1024];
        InputStreamReader reader = new InputStreamReader(input);
        int chars = reader.read(buffer);
        while(chars > 0) {
            writer.write(buffer, 0, chars);
            chars = reader.read(buffer);
        }
        reader.close();
    }

    public static StringWriter asString(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        char[] buffer = new char[1024];
        InputStreamReader reader = new InputStreamReader(input);
        int chars = reader.read(buffer);
        while(chars > 0) {
            writer.write(buffer, 0, chars);
            chars = reader.read(buffer);
        }
        reader.close();
        return writer;
    }
}
