package com.peregrine.generator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class App 
{

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final int DEFAULT_PAGECOUNT = 50;
    private static final int DEFAULT_IMAGECOUNT = 50;
    private static final int DEFAULT_FOLDERCOUNT = 10;
    private static final int DEFAULT_PAGEDEPTH = 5;
    private static final int DEFAULT_IMAGEDEPTH = 3;


    public static void main( String[] args ) throws Exception
    {
        Options options = new Options();
        options.addOption("h", "hostname", true, "Sling server hostname");
        options.addOption("p", "port", true, "Sling server port");
        options.addOption("c", "pagecount", true, "Number of pages to create");
        options.addOption("C", "imagecount", true, "Number of images to create");
        options.addOption("f", "foldercount", true, "Number of asset folders");
        options.addOption("U", "username", true, "Sling server username");
        options.addOption("P", "password", true, "Sling server password");
        options.addOption("d", "pagedepth", true, "Maximum depth of created pages");
        options.addOption("D", "imagedepth", true, "Maximum depth of created image folders");

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

        int imagecount;
        if(cmd.hasOption("C"))
        {
            String imagecountString = cmd.getOptionValue("C");
            try
            {
                imagecount = Integer.parseInt(imagecountString);
            }
            catch(Exception e)
            {
                imagecount = DEFAULT_IMAGECOUNT;
            }
        }
        else
        {
            imagecount = DEFAULT_IMAGECOUNT;
        }

        int foldercount;
        if(cmd.hasOption("f"))
        {
            String foldercountString = cmd.getOptionValue("f");
            try
            {
                foldercount = Integer.parseInt(foldercountString);
            }
            catch(Exception e)
            {
                foldercount = DEFAULT_FOLDERCOUNT;
            }
        }
        else
        {
            foldercount = DEFAULT_FOLDERCOUNT;
        }

        int pagedepth;
        if(cmd.hasOption("d"))
        {
            String pagedepthString = cmd.getOptionValue("d");
            try
            {
                pagedepth = Integer.parseInt(pagedepthString);
            }
            catch (Exception e)
            {
                pagedepth = DEFAULT_PAGEDEPTH;
            }
        }
        else
        {
            pagedepth = DEFAULT_PAGEDEPTH;
        }

        int imagedepth;
        if(cmd.hasOption("D"))
        {
            String imagedepthString = cmd.getOptionValue("D");
            try
            {
                imagedepth = Integer.parseInt(imagedepthString);
            }
            catch (Exception e)
            {
                imagedepth = DEFAULT_IMAGEDEPTH;
            }
        }
        else
        {
            imagedepth = DEFAULT_IMAGEDEPTH;
        }

        String encodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String host = "http://" + hostname + ":" + port;
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try
        {
            PageCreator pageCreator = new PageCreator(encodedCredentials, host, httpClient);
            ImageCreator imageCreator = new ImageCreator(encodedCredentials, host, httpClient);
            long now = System.currentTimeMillis();
            String basePath = "/content/sites/example/generated-" + now;
            String rootPagePath = pageCreator.createPage(basePath, "Generated Content " + (new Date(now)).toString());

            createPages(rootPagePath, pagecount, pagedepth, pageCreator);

//            basePath += "/autopage";
//
//
//
//            for(int i = 1; i <= pagecount; i++)
//            {
//                pageCreator.createPage(basePath + i, "Auto Page " + i);
//            }




            String baseAssetPath = "/content/assets";
            String generatedRootFolderName = "generated-" + now;

            String rootFolderPath = imageCreator.createFolder(baseAssetPath, generatedRootFolderName);
            List<String> folderPaths = createFolders(rootFolderPath, foldercount, imagedepth, imageCreator);
            List<String> imagePaths = new ArrayList<String>();

            for(int i = 0; i < imagecount; i++)
            {
                String folderPath = folderPaths.get(ThreadLocalRandom.current().nextInt(0, folderPaths.size()));
                imagePaths.add(imageCreator.createImage(folderPath, "image-" + i + ".png"));
            }

            List<String> columnPaths = pageCreator.getColumnPaths();
            if(columnPaths != null && !columnPaths.isEmpty())
            {
                if(!imagePaths.isEmpty())
                {
                    for(int i = 0; i < imagePaths.size(); i++)
                    {
                        String columnPath = columnPaths.get(ThreadLocalRandom.current().nextInt(0, columnPaths.size()));
                        String imageComponentPath = columnPath + "/image" + i;
                        String title = "Generated Image Component " + i;
                        String captipon = "Image #" + i;
                        pageCreator.createImageComponent(imageComponentPath, imagePaths.get(i), title, captipon);
                    }
                }
            }

        }
        finally {
            httpClient.close();
        }
    }

    public static void createPages(String rootPagePath, int pagecount, int pagedepth, PageCreator pageCreator) throws Exception
    {
        int pagesCreated = 0;

        List<String> currentLevelPaths = new ArrayList<>();
        currentLevelPaths.add(rootPagePath);
        for(int currentDepth = 0; (currentDepth < pagedepth) && (pagesCreated < pagecount); currentDepth++)
        {
            int maxAtDepth;
            if(currentDepth == pagedepth -1)
            {
                maxAtDepth = pagecount - pagesCreated;
            }
            else
            {
                maxAtDepth = ThreadLocalRandom.current().nextInt(1, (pagecount / pagedepth) + 1);
            }
            List<String> createdPaths = new ArrayList<>();
            for(int countAtDepth = 0; (countAtDepth < maxAtDepth) && (pagesCreated < pagecount); countAtDepth++)
            {
                String parentPath = currentLevelPaths.get(ThreadLocalRandom.current().nextInt(0, currentLevelPaths.size()));
                String suffix = "d"+currentDepth+"f"+countAtDepth;
                String createdPath = pageCreator.createPage(parentPath + "/" + suffix, "Auto-Generated Page " + suffix);
                createdPaths.add(createdPath);

                pagesCreated++;
            }
            currentLevelPaths = new ArrayList<>(createdPaths);
        }
    }

    public static List<String> createFolders(String rootFolderPath, int foldercount, int imagedepth, ImageCreator imageCreator) throws Exception
    {
        List<String> folderPaths = new ArrayList<>();

        folderPaths.add(rootFolderPath);
        int foldersCreated = 0;

        List<String> currentLevelPaths = new ArrayList<>();
        currentLevelPaths.add(rootFolderPath);
        for(int currentDepth = 0; (currentDepth < imagedepth) && (foldersCreated < foldercount); currentDepth++)
        {
            int maxAtDepth;
            if(currentDepth == imagedepth -1)
            {
                maxAtDepth = foldercount - foldersCreated;
            }
            else
            {
                maxAtDepth = ThreadLocalRandom.current().nextInt(1, (foldercount / imagedepth) + 1);
            }
            List<String> createdPaths = new ArrayList<>();
            for(int countAtDepth = 0; (countAtDepth < maxAtDepth) && (foldersCreated < foldercount); countAtDepth++)
            {
                String parentPath = currentLevelPaths.get(ThreadLocalRandom.current().nextInt(0, currentLevelPaths.size()));
                String createdPath = imageCreator.createFolder(parentPath, "d"+currentDepth+"f"+countAtDepth);
                createdPaths.add(createdPath);
                folderPaths.add(createdPath);
                foldersCreated++;
            }
            currentLevelPaths = new ArrayList<>(createdPaths);
        }

        return folderPaths;
    }

}
