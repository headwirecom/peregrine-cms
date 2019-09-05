package com.peregrine.nodejs.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Andreas Schaefer on 4/20/17.
 */
public class Activator
    implements BundleActivator
{
    public static final String NPM_PATH = "node_modules/npm";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static BundleContext bundleContext;

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        // Check if NPM folder is installed
        File nodesParentFolder = getNodesParentFolder();
        if(nodesParentFolder != null) {
            File npmFolder = new File(nodesParentFolder, NPM_PATH);
            if(npmFolder.exists() && npmFolder.isDirectory()) {
                // We are fine here
                log.debug("NPM Folder exists: '{}'", npmFolder.getAbsoluteFile());
            } else {
                extractAndInstallNPM(npmFolder);
            }
        }
    }

    public void stop(BundleContext context) throws Exception {
        bundleContext = null;
    }

    private File getNodesParentFolder() {
        File answer = null;
        // Check if NPM is installed and if not export and install locally
        File homeFolder = new File(System.getProperty("user.home"));
        if(homeFolder.exists() && homeFolder.isDirectory()) {
            answer = homeFolder;
        }
        return answer;
    }

    private void extractAndInstallNPM(File npmFolder) {
        if(npmFolder.mkdirs()) {
            try {
                InputStream stream = getClass().getClassLoader().getResourceAsStream("npm-4.5.0.zip");
                unzip(npmFolder, stream);
                log.debug("NPM successfully installed to '{}'", npmFolder.getAbsolutePath());
            } catch(IOException e) {
                log.error("Failed to export NPM file: '{}'", npmFolder.getPath(), e);
            }
        } else {
            log.error("Failed to create npmFolder: '{}'", npmFolder.getPath());
        }
    }

    private static final int BUFFER_SIZE = 4096;

    /**
     * Extracts the ZIP Input Stream into the given folder
     *
     * @param zipFileStream Input Stream of the ZIP file resource
     * @param destFolder target folder where to install the ZIP extract to
     * @throws IOException If the extractions failed
     */
    private void unzip(File destFolder, InputStream zipFileStream) throws IOException {
        if (!destFolder.exists()) {
            destFolder.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(zipFileStream);
        ZipEntry entry = zipIn.getNextEntry();
        String canonicalDestPath = destFolder.getCanonicalPath();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destFolder.getPath() + File.separator + entry.getName();
            File entryFile = new File(filePath);
            String canonicalTargetPat = entryFile.getCanonicalPath();
            if (!canonicalTargetPat.startsWith(canonicalDestPath + File.separator)) {
                throw new IOException("ZIP Entry is trying to leave target dir: " + entry.getName());
            }
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                entryFile.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry of a file
     *
     * @param zipIn Zip File Entry stream
     * @param filePath Path to install the file into
     * @throws IOException If the extraction failed
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
