package com.peregrine.felib.models;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * Created by rr on 4/18/2017.
 */
public class JCRFile {

    private String filePath;
    private String data;

    public JCRFile(String filePath, String data) {
        this.filePath = filePath;
        this.data = data;
    }

    public String getPath() {
        return filePath;
    }

    public String getContent() {
        return data;
    }

    public String toString() {
        return "filePath: "+filePath;
    }

}
