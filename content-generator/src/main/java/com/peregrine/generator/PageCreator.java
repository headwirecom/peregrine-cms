package com.peregrine.generator;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by headwire on 5/11/2017.
 */
public class PageCreator {

    private String encodedCredentials;
    private String host;
    private CloseableHttpClient httpClient;

    public PageCreator(String encodedCredentials, String host, CloseableHttpClient httpClient)
    {
        this.encodedCredentials = encodedCredentials;
        this.host = host;
        this. httpClient = httpClient;
    }

    public void createPage(String path, String title) throws Exception
    {
        System.out.println("Creating page at " + path);
        String url = host + path;
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("jcr:primaryType", "per:Page"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            HttpEntity responseEntity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 201)
            {
                System.out.println("Successfully created page at " + path);
                createContentNode(path + "/jcr:content", title);
            }
            else
            {
                System.out.println("Failed to create page at " + path);
                System.out.println(statusLine);
            }
            EntityUtils.consume(responseEntity);
        }
        finally {
            response.close();
        }
    }

    private void createContentNode(String path, String title) throws Exception
    {
        System.out.println("Creating content node at " + path);
        String url = host + path;
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("jcr:primaryType", "per:PageContent"));
        formParams.add(new BasicNameValuePair("sling:resourceType", "example/components/page"));
        formParams.add(new BasicNameValuePair("template", "/content/templates/example"));
        if(title != null && !title.isEmpty())
        {
            formParams.add(new BasicNameValuePair("jcr:title", title));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            HttpEntity responseEntity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 201)
            {
                System.out.println("Successfully created content node at " + path);
                createContainer(path + "/content");
            }
            else
            {
                System.out.println("Failed to create content node at " + path);
                System.out.println(statusLine);
            }
            EntityUtils.consume(responseEntity);
        }
        finally {
            response.close();
        }

    }

    private void createContainer(String path) throws Exception
    {
        System.out.println("Creating container node at " + path);
        String url = host + path;
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("jcr:primaryType", "nt:unstructured"));
        formParams.add(new BasicNameValuePair("sling:resourceType", "pagerender/vue/structure/container"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            HttpEntity responseEntity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 201)
            {
                System.out.println("Successfully created container node at " + path);
            }
            else
            {
                System.out.println("Failed to create container node at " + path);
                System.out.println(statusLine);
            }
            EntityUtils.consume(responseEntity);
        }
        finally {
            response.close();
        }
    }

}
