package com.peregrine.generator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.Base64;
import java.util.Date;

public class App 
{

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final int DEFAULT_PAGECOUNT = 5;

    public static void main( String[] args ) throws Exception
    {
        Options options = new Options();
        options.addOption("h", "hostname", true, "Sling server hostname");
        options.addOption("p", "port", true, "Sling server port");
        options.addOption("c", "pagecount", true, "Number of pages to create");
        options.addOption("U", "username", true, "Sling server username");
        options.addOption("P", "password", true, "Sling server password");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String hostname = cmd.hasOption("h") ? cmd.getOptionValue("h") : DEFAULT_HOSTNAME;
        String port = cmd.hasOption("p") ? cmd.getOptionValue("p") : DEFAULT_PORT;
        String username = cmd.hasOption("U") ? cmd.getOptionValue("U") : DEFAULT_USERNAME;
        String password = cmd.hasOption("P") ? cmd.getOptionValue("P") : DEFAULT_PASSWORD;

        int pagecount;
        if(cmd.hasOption("c"))
        {
            String pagecountString = cmd.getOptionValue("c");
            try
            {
                pagecount = Integer.parseInt(pagecountString);
            }
            catch(Exception e)
            {
                pagecount = DEFAULT_PAGECOUNT;
            }
        }
        else
        {
            pagecount = DEFAULT_PAGECOUNT;
        }

        String encodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String host = "http://" + hostname + ":" + port;
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try
        {
            PageCreator pageCreator = new PageCreator(encodedCredentials, host, httpClient);
            long now = System.currentTimeMillis();
            String basePath = "/content/sites/example/generated-" + now;
            pageCreator.createPage(basePath, "Generated Content " + (new Date(now)).toString());
            basePath += "/autopage";
            for(int i = 1; i <= pagecount; i++)
            {
                pageCreator.createPage(basePath + i, "Auto Page " + i);
            }
        }
        finally {
            httpClient.close();
        }
    }
}
