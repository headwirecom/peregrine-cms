package com.peregrine.nodejs.process;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Process Tracker Implementation based on local Files
 * Created by Andreas Schaefer on 4/5/17.
 */
public class ProcessContextReader
    implements ProcessContext
{
    private final Logger log = LoggerFactory.getLogger(ProcessContextReader.class);

    private int exitCode;
    private File outputFile;
    private File errorFile;

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public ProcessContextReader setExitCode(int exitCode) {
        this.exitCode = exitCode;
        return this;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public ProcessContextReader setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public File getErrorFile() {
        return errorFile;
    }

    public ProcessContextReader setErrorFile(File errorFile) {
        this.errorFile = errorFile;
        return this;
    }

    /**
     * Deletes the given files and its parent if there is a file
     */
    public void tearDown() {
        File parent = null;
        if(outputFile != null) {
            parent = outputFile.getParentFile();
        } else if(errorFile != null){
            parent = errorFile.getParentFile();
        }
        if(errorFile != null) {
            if(!errorFile.delete()) {
                log.warn("Failed to delete External Process Error File: " + errorFile.getAbsolutePath());
            }
        }
        if(outputFile != null) {
            if(!outputFile.delete()) {
                log.warn("Failed to delete External Process Output File: " + outputFile.getAbsolutePath());
            }
        }
        if(parent != null) {
            if(!parent.delete()) {
                log.warn("Failed to delete External Process Folder: " + parent.getAbsolutePath());
            }
        }
    }

    @Override
    public String getOutput() {
        return getFileContent(outputFile);
    }

    @Override
    public Reader getOutputReader() {
        try {
            return new FileReader(outputFile);
        } catch(FileNotFoundException e) {
            // Should not happen
            return null;
        }
    }

    @Override
    public String getError() {
        return getFileContent(errorFile);
    }

    @Override
    public Reader getErrorReader() {
        try {
            return new FileReader(errorFile);
        } catch(FileNotFoundException e) {
            // Should not happen
            return null;
        }
    }

    private String getFileContent(File file) {
        String answer = "";
        if(file != null) {
            StringWriter writer = null;
            FileReader reader = null;
            try {
                writer = new StringWriter();
                reader = new FileReader(file);
                IOUtils.copy(reader, writer);
            } catch(FileNotFoundException e) {
                log.error("Failed to find file: '" + file + "' (should not happen)", e);
            } catch(IOException e) {
                log.error("Failed to read file: '" + file + "' (should not happen)", e);
            } finally {
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(writer);
            }
            answer = writer.toString();
        } else {
            log.warn("File it not specified to read from");
        }
        return answer;
    }
}
