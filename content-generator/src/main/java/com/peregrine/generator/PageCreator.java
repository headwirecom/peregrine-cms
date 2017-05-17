package com.peregrine.generator;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by headwire on 5/11/2017.
 */
public class PageCreator {

    private static final Logger LOG = LoggerFactory.getLogger(PageCreator.class);

    private String encodedCredentials;
    private String host;
    private CloseableHttpClient httpClient;

    public List<String> getColumnPaths() {
        return Collections.unmodifiableList(columnPaths);
    }

    private List<String> columnPaths;

    private List<Integer> columnCountOptions;
    private Map<Integer, String> columnSizeMap;

    private Lorem lorem;

    public PageCreator(String encodedCredentials, String host, CloseableHttpClient httpClient)
    {
        this.encodedCredentials = encodedCredentials;
        this.host = host;
        this. httpClient = httpClient;

        columnSizeMap = new HashMap<>();
        columnSizeMap.put(1, "col-md-12");
        columnSizeMap.put(2, "col-md-6");
        columnSizeMap.put(3, "col-md-4");
        columnSizeMap.put(4, "col-md-3");
        columnSizeMap.put(6, "col-md-2");
        columnSizeMap.put(12, "col-md-1");

        columnCountOptions = new ArrayList<>(columnSizeMap.keySet());

        lorem = LoremIpsum.getInstance();

        columnPaths = new ArrayList<>();
    }

    public String createPage(String path, String title) throws Exception
    {
        LOG.trace("Creating page at {}", path);
        String url = host + path;
        HttpPost httpPost = createPost(url,
                new BasicNameValuePair("jcr:primaryType", "per:Page"));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created page at {}", path);
                createContentNode(path + "/jcr:content", title);
                return path;
            }
            else
            {
                LOG.error("Failed to create page at {} : status code {}", path, statusCode);
                throw new RuntimeException();
            }
        }
        finally {
            response.close();
        }
    }

    private void createContentNode(String path, String title) throws Exception
    {
        LOG.trace("Creating content node at {}", path);
        String url = host + path;
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("jcr:primaryType", "per:PageContent"));
        formParams.add(new BasicNameValuePair("sling:resourceType", "example/components/page"));
        formParams.add(new BasicNameValuePair("template", "/content/templates/example"));
        if(title != null && !title.isEmpty())
        {
            formParams.add(new BasicNameValuePair("jcr:title", title));
        }
        HttpPost httpPost = createPost(url, formParams.toArray(new BasicNameValuePair[formParams.size()]));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created content node at {}", path);
                createContainer(path + "/content");
            }
            else
            {
                LOG.error("Failed to create content node at {} : status code {}", path, statusCode);
            }
        }
        finally {
            response.close();
        }

    }

    public void createContainer(String path) throws Exception
    {
        LOG.trace("Creating container node at {}", path);
        String url = host + path;
        HttpPost httpPost = createPost(url,
                new BasicNameValuePair("jcr:primaryType", "nt:unstructured"),
                new BasicNameValuePair("sling:resourceType", "pagerender/vue/structure/container"));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created container node at {}", path);
                fillPageWithContent(path);
            }
            else
            {
                LOG.error("Failed to create container node at {} : status code {}", path, statusCode);
            }
        }
        finally {
            response.close();
        }
    }

    public void fillPageWithContent(String path) throws Exception
    {
        LOG.trace("Filling page with content at {}", path);
        int rowCount = ThreadLocalRandom.current().nextInt(1, 6);
        for(int i = 1; i <= rowCount; i++)
        {
            createRow(path + "/row" + i);
        }
    }

    public void createRow(String path) throws Exception
    {
        LOG.trace("Creating row node at {}", path);
        String url = host + path;
        HttpPost httpPost = createPost(url,
                new BasicNameValuePair("jcr:primaryType", "nt:unstructured"),
                new BasicNameValuePair("sling:resourceType", "example/components/row"));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created row at {}", path);
                int columnCount = columnCountOptions.get(ThreadLocalRandom.current().nextInt(0, columnCountOptions.size()));
                for(int i = 1; i <= columnCount; i++)
                {
                    createColumn(path + "/col" + i, columnSizeMap.get(columnCount));
                }
            }
            else
            {
                LOG.error("Failed to create row at {} : status code {}", path, statusCode);
            }
        }
        finally {
            response.close();
        }
    }

    public void createColumn(String path, String colClass) throws Exception
    {
        LOG.trace("Creating column node at {}", path);
        String url = host + path;
        HttpPost httpPost = createPost(url,
                new BasicNameValuePair("jcr:primaryType", "nt:unstructured"),
                new BasicNameValuePair("sling:resourceType", "example/components/col"),
                new BasicNameValuePair("classes", colClass));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created column at {}", path);
                int paragraphCount = ThreadLocalRandom.current().nextInt(1, 6);
                for(int i = 1; i <= paragraphCount; i++)
                {
                    createText(path + "/text" + i);
                }
                columnPaths.add(path);
            }
            else
            {
                LOG.error("Failed to create column at {} : status code {}", path, statusCode);
            }
        }
        finally {
            response.close();
        }
    }

    public void createText(String path) throws Exception
    {
        LOG.trace("Creating text node at {}", path);
        String url = host + path;
        String text = lorem.getWords(100, 300);
        HttpPost httpPost = createPost(url,
                new BasicNameValuePair("jcr:primaryType", "nt:unstructured"),
                new BasicNameValuePair("sling:resourceType", "pagerender/vue/components/base"),
                new BasicNameValuePair("text", text));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created text at {}", path);
            }
            else
            {
                LOG.error("Failed to create text at {} : statusCode {}", path, statusCode);
            }
        }
        finally {
            response.close();
        }
    }

    public void createImageComponent(String path, String imagePath, String title, String caption) throws Exception
    {
        LOG.trace("Creating image component node at {}", path);
        String url = host + path;
        HttpPost httpPost = createPost(url,
                new BasicNameValuePair("jcr:primaryType", "nt:unstructured"),
                new BasicNameValuePair("sling:resourceType", "example/components/image"),
                new BasicNameValuePair("imagePath", imagePath),
                new BasicNameValuePair("title", title),
                new BasicNameValuePair("caption", caption));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            int statusCode = consumeResponse(response);
            if(statusCode == 201)
            {
                LOG.trace("Successfully created image component at {}", path);
            }
            else
            {
                LOG.error("Failed to create image component at {} : statusCode {}", path, statusCode);
            }
        }
        finally {
            response.close();
        }
    }

    public static int consumeResponse(HttpResponse response) throws Exception
    {
        HttpEntity responseEntity = response.getEntity();
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        EntityUtils.consume(responseEntity);
        return statusCode;
    }

    public HttpPost createPost(String url, BasicNameValuePair... formData)
    {
        HttpPost httpPost = new HttpPost(url);
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Arrays.asList(formData), Consts.UTF_8);
        httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
        httpPost.setEntity(entity);
        return httpPost;
    }

}
