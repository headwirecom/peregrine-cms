package com.peregrine.felib.models;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by rr on 4/18/2017.
 */
public class FELibModel {

    @Self
    private Resource node;

    public ArrayList<JCRFile> getFiles(String fileName) {
        ArrayList<JCRFile> ret = new ArrayList<JCRFile>();
        ResourceResolver rr = node.getResourceResolver();
        Resource definitionNode = node.getChild(fileName);
        if(definitionNode != null) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(definitionNode.adaptTo(InputStream.class)));

                String basePath = node.getPath();
                String line = br.readLine();
                while (line != null) {
                    if (!(line.startsWith("#") || line.trim().length() == 0)) {
                        if(line.startsWith("base=")) {
                            String path = line.substring(5);
                            basePath = node.getPath()+"/"+path;
                        } else {
                            String filePath = basePath+"/"+line;
                            Resource file = rr.getResource(filePath);
                            if(file != null) {
                                InputStream is = file.adaptTo(InputStream.class);
                                String data = IOUtils.toString(is, "UTF-8");
                                ret.add(new JCRFile(basePath + "/" + line, data));
                            }
                        }
                    }
                    line = br.readLine();
                }

            } catch (IOException ioe) {
                throw new RuntimeException("failed to read file", ioe);
            } finally {
                try {
                    if (br != null) br.close();
                } catch (IOException ioe) {
                    throw new RuntimeException("failed to clean up file", ioe);
                }
            }
        }
        return ret;
    }

}
