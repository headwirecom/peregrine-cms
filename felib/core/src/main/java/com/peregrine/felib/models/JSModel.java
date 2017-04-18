package com.peregrine.felib.models;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by rr on 4/17/2017.
 */

@Model(adaptables=Resource.class)
public class JSModel extends FELibModel {

    public ArrayList<JCRFile> getFiles() {
        return super.getFiles("js.txt");
    }

}



