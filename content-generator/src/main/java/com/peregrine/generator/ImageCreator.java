package com.peregrine.generator;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by headwire on 5/15/2017.
 */
public class ImageCreator {

    private static final Logger LOG = LoggerFactory.getLogger(ImageCreator.class);

    private static final String FILE_SERVLET_PATH = "/api/admin/uploadFiles.json";
    private static final String FOLDER_SERVLET_PATH = "/api/admin/createFolder.json";

    private String encodedCredentials;
    private String host;
    private CloseableHttpClient httpClient;

    public ImageCreator(String encodedCredentials, String host, CloseableHttpClient httpClient)
    {
        this.encodedCredentials = encodedCredentials;
        this.host = host;
        this.httpClient = httpClient;
    }

    public String createFolder(String path, String name) throws Exception
    {
        LOG.trace("Creating folder at {}/{}", path, name);
        String url = host + FOLDER_SERVLET_PATH + "/path//" + path + "//name//" + name;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic " + encodedCredentials);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            int statusCode = PageCreator.consumeResponse(response);
            if(statusCode == 200)
            {
                LOG.trace("Successfully created folder at {}/{}", path, name);
                return path + "/" + name;
            }
            else
            {
                LOG.error("Failed to create folder at {} : status code {}", path + "/" + name, statusCode);
                throw new RuntimeException();
            }
        }
        finally {
            response.close();
        }
    }

    public File generateImage(String filename) throws Exception
    {
        int width = 200;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                int a = (int)(Math.random()*256); //alpha
                int r = (int)(Math.random()*256); //red
                int g = (int)(Math.random()*256); //green
                int b = (int)(Math.random()*256); //blue

                int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                image.setRGB(x, y, p);
            }
        }

        File f = new File(filename);
        f.deleteOnExit();
        ImageIO.write(image, "png", f);
        return f;
    }

    public String createImage(String path, String filename) throws Exception
    {
        LOG.trace("Creating image at {}/{}", path, filename);
        String url = host + FILE_SERVLET_PATH + "/path//" + path;
        File f = generateImage(filename);
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody(filename, f, ContentType.DEFAULT_BINARY, filename);
        HttpEntity entity = builder.build();
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try
        {
            int statusCode = PageCreator.consumeResponse(response);
            if(statusCode == 200)
            {
                LOG.trace("Successfully created image at {}/{}", path, filename);
                return path + "/" + filename;
            }
            else
            {
                LOG.error("Failed to create image at {} : status code {}", path + "/" + filename, statusCode);
                throw new RuntimeException();
            }
        }
        finally {
            response.close();
        }
    }

}
