package com.peregrine.felib.models;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by rr on 4/17/2017.
 */

@Model(adaptables=Resource.class)
public class CSSModel extends FELibModel {

    public ArrayList<JCRFile> getFiles() {
        return super.getFiles("css.txt");
    }
}



