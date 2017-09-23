package com.peregrine.nodejs.process;

import com.peregrine.nodejs.j2v8.J2V8ProcessExecution;
import com.peregrine.nodejs.j2v8.J2V8WebExecution;
import com.peregrine.nodejs.j2v8.ScriptException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static com.peregrine.nodejs.process.ProcessContextReader.NO_EXIT_CODE;

/**
 * Created by Andreas Schaefer on 4/6/17.
 */
public class ProcessRunner {

    private final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

    private File workingDirectory = new File(".");

    public ProcessRunner() {}

    public ProcessRunner(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public boolean isWindows() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        return OS.startsWith("win");
    }

    public ProcessContext execute(List<String> command)
        throws ExternalProcessException
    {
        log.trace("Execute Command: '{}'", command);
        Path dir = null;
        try {
            dir = Files.createTempDirectory(null);
        } catch(IOException e) {
            throw new ExternalProcessException("Failed to create temporary directory to catch output", e);
        }

        final File output = dir.resolve("output.txt").toFile();
        output.deleteOnExit();
        final File error = dir.resolve("error.txt").toFile();
        error.deleteOnExit();
        log.trace("Output File: '{}', Error File: '{}'", output.getPath(), error.getPath());
        ProcessContextReader answer = new ProcessContextReader().setOutputFile(output).setErrorFile(error);

        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDirectory);
        pb.redirectOutput(Redirect.to(output));
        pb.redirectError(Redirect.to(error));

        int exitCode = NO_EXIT_CODE;
        try {
            final Process p = pb.start();
            try {
                exitCode = p.waitFor();
                log.trace("Exit Code: '{}'", exitCode);
            } catch(final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch(IOException e) {
            log.trace("IO Exception", e);
            throw new ExternalProcessException("Failed to execute process", e).setCommand(command);
        }

        return answer.setExitCode(exitCode);
    }

    public ProcessContext executeWithJ2V8(J2V8ProcessExecution executor, String scriptJcrPath, List<String> command)
        throws ExternalProcessException
    {
        ProcessContextTracker answer = new ProcessContextTracker();
        if(executor != null) {
            try {
                executor.executeScript(
                    scriptJcrPath,
                    answer,
                    command
                );
            } catch(ScriptException e) {
                throw new ExternalProcessException("Failed to List Packages", e).setCommand(command).setProcessContext(answer);
            }
        }

        return answer;
    }

    public ProcessContext executeWithJ2V8(J2V8WebExecution executor, String scriptJcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ExternalProcessException
    {
        ProcessContextTracker answer = new ProcessContextTracker();
        if(executor != null) {
            try {
                executor.executeScript(
                        scriptJcrPath,
                        request,
                        response
                );
            } catch(ScriptException e) {
                throw new ExternalProcessException("Failed to List Packages", e).setProcessContext(answer);
            }
        }

        return answer;
    }
}
