package com.peregrine.admin.transform.operation;

import com.peregrine.admin.process.ExternalProcessException;
import com.peregrine.admin.process.ProcessContext;
import com.peregrine.admin.process.ProcessRunner;
import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by schaefa on 5/19/17.
 */
public abstract class AbstractVipsImageTransformation
    implements ImageTransformation
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected void transform0(ImageContext imageContext, String operationName, String...parameters)
        throws TransformationException
    {
        String name = "transformation.in." + System.currentTimeMillis() + "." + imageContext.getImageType();
        Path temporaryFolder = createTempFolder();
        if(temporaryFolder != null) {
            File input = writeToFile(temporaryFolder, name, imageContext.getImageStream());
            if(input == null) {
                throw new TransformationException("Could not create input file: " + name);
            }
            String outputFileName = "transformation.out." + System.currentTimeMillis() + "." + imageContext.getOutputImageType();
            File output = createTempFile(temporaryFolder, outputFileName);
            if(output == null) {
                throw new TransformationException("Could not create output file: " + outputFileName);
            }
            ProcessRunner runner = new ProcessRunner();
            List<String> commands = new ArrayList<>(Arrays.asList("vips", operationName));
            boolean inputUsed = false, outputUsed = false;
            for(String parameter: parameters) {
                if("{in}".equals(parameter)) {
                    commands.add(input.getAbsolutePath());
                    inputUsed = true;
                } else
                if("{out}".equals(parameter)) {
                    commands.add(output.getAbsolutePath());
                    outputUsed = true;
                } else {
                    commands.add(parameter);
                }
            }
            if(!inputUsed) {
                throw new IllegalArgumentException("Input was not used ({in})");
            }
            InputStream inputStream = null;
            try {
                ProcessContext processContext = runner.execute(commands);
                if(processContext.getExitCode() > 0) {
                    throw new TransformationException("Failed to execute VIPS Operation: " + operationName + " with exit code: " + processContext.getExitCode());
                }
                if(outputUsed) {
                    inputStream = openFileInput(temporaryFolder, output.getName());
                } else {
                    inputStream = openFileInput(temporaryFolder, input.getName());
                }
                imageContext.resetImageStream(inputStream);
            } catch(ExternalProcessException e) {
                log.error("Failed to execute VIPS", e);
                throw new TransformationException("Failed to execute VIPS Operation: " + operationName, e);
            }
        } else {
            throw new TransformationException("Could not create temporary folder: " + name);
        }
    }

    private Path createTempFolder() {
        Path dir = null;
        try {
            dir = Files.createTempDirectory(null);
            dir.toFile().deleteOnExit();
        } catch(IOException e) {
            log.error("Failed to create temporary folder", e);
        }
        return dir;
    }

    private File createTempFile(Path tempFolder, String fileName) {
        File file = null;
        try {
            file = tempFolder.resolve(fileName).toFile();
            file.deleteOnExit();
        } catch(InvalidPathException e) {
            log.error("Failed to create temporary file", e);
        }
        return file;
    }

    /**
     * Writes the given input stream into the given file
     * @param directory Folder where the file is placed into
     * @param fileName File Name of the file to be created including extension
     * @param inputStream Data that is written to the file. This Input Stream is closed at the end
     * @return
     */
    private File writeToFile(Path directory, String fileName, InputStream inputStream) {
        File output = null;
        FileOutputStream fos = null;
        try {
            output = directory.resolve(fileName).toFile();
            output.deleteOnExit();
            fos = new FileOutputStream(output);
            IOUtils.copy(inputStream, fos);
        } catch(InvalidPathException e) {
            log.error("Failed to create Output File", e);
        } catch(FileNotFoundException e) {
            log.error("Failed to create File Output Stream", e);
            output.delete();
            output = null;
        } catch(IOException e) {
            log.error("Failed to write to file", e);
            output.delete();
            output = null;
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(inputStream);
        }
        return output;
    }

    private InputStream openFileInput(Path directory, String fileName) {
        File output = null;
        FileInputStream fis = null;
        try {
            output = directory.resolve(fileName).toFile();
            if(output.isFile() && output.exists()) {
                output.deleteOnExit();
                fis = new FileInputStream(output);
            } else {
                log.error("File: '{}' does not exist", output.getPath());
            }
        } catch(FileNotFoundException e) {
            log.error("Failed to write to file", e);
            e.printStackTrace();
        }
        return fis;
    }

}
