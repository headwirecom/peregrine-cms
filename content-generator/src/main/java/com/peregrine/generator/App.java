package com.peregrine.generator;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Encoder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
//    public static void main( String[] args ) throws Exception
//    {
//        String encodedCredentials = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
//
//        String url = "http://localhost:8080/content/sites/example/testpage5";
//        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("jcr:primaryType", "per:Page"));
//        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
//        HttpPost httpPost = new HttpPost(url);
//        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
//        httpPost.setEntity(entity);
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//
//        try
//        {
//            CloseableHttpResponse response = httpClient.execute(httpPost);
//            try
//            {
//                System.out.println(response.getStatusLine());
//                EntityUtils.consume(entity);
//            }
//            finally {
//                response.close();
//            }
//        }
//        finally {
//            httpClient.close();
//        }
//
//    }

    public static void main( String[] args ) throws Exception
    {
        String encodedCredentials = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
        String host = "http://localhost:8080";
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try
        {
            PageCreator pageCreator = new PageCreator(encodedCredentials, host, httpClient);
            String basePath = "/content/sites/example/autopage";
            for(int i = 0; i < 5; i++)
            {
                pageCreator.createPage(basePath + i, "Auto Page " + i);
            }
        }
        finally {
            httpClient.close();
        }
    }
}
