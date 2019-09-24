package com.peregrine.nodejs.npm;

import com.peregrine.nodejs.j2v8.J2V8ProcessExecution;
import com.peregrine.nodejs.process.ProcessRunner;
import com.peregrine.process.ExternalProcessException;
import com.peregrine.process.ProcessContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service that Handles NPM Packages
 *
 * Created by Andreas Schaefer on 4/4/17.
 */
@Component(
    service = NpmExternalProcess.class,
    immediate = true
)
@Designate(
    ocd = NpmExternalProcessConfiguration.class
)
public class NpmExternalProcessService
    implements NpmExternalProcess
{
    public static final String NPM_LIST_SCRIPT_PATH = "/apps/nodejs/scripts/npm-list.js";
    public static final String NPM_INSTALL_SCRIPT_PATH = "/apps/nodejs/scripts/npm-install.js";
    public static final String NPM_REMOVE_SCRIPT_PATH = "/apps/nodejs/scripts/npm-remove.js";

    private final Logger log = LoggerFactory.getLogger(NpmExternalProcessService.class);

    private File workingDirectory = new File(".");
    private boolean active;

    private ProcessRunner processRunner = new ProcessRunner();

    private J2V8ProcessExecution executor;

    @Reference(
        cardinality = ReferenceCardinality.OPTIONAL,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    void bindJ2V8ProcessExecution(J2V8ProcessExecution executor) {
        log.trace("Bind J2V8 Process Execution: '{}'", executor);
        this.executor = executor;
    }
    void unbindJ2V8ProcessExecution(J2V8ProcessExecution executor) {
        log.trace("Unbind J2V8 Process Execution: '{}'", executor);
        this.executor = null;
    }

    /**
     * Activate this component.
     * Start the scheduler.
     * @throws ExternalProcessException
     */
    @Activate
    protected void activate(NpmExternalProcessConfiguration configuration)
        throws ExternalProcessException
    {
        String folderPath = configuration.npmRepositoryFolderPath();
        File folder = new File(folderPath);
        if(!folder.exists()) {
            throw new ExternalProcessException("Folder Path: '" + folderPath + "' does not exist");
        }
        if(!folder.isDirectory()) {
            throw new ExternalProcessException("Folder Path: '" + folderPath + "' is not a folder");
        }
        workingDirectory = folder;
        this.active = true;
    }

    @Modified
    protected void modified(NpmExternalProcessConfiguration configuration)
        throws ExternalProcessException
    {
        activate(configuration);
    }

    /**
     * Deactivate this component.
     * Stop the scheduler.
     */
    @Deactivate
    protected void deactivate() {
        this.active = false;
    }

    @Override
    public ProcessContext listPackages(boolean withJ2V8, String name, int depth, String type, int size)
        throws ExternalProcessException
    {
        if(!active) {
            throw new ExternalProcessException("Service is not active");
        }

        checkNodeModulesFolder();

        List<String> command = createProcessCommand(withJ2V8, PROCESS_LIST);

        if(depth >= 0) {
            command.add(PARAMETER_DEPTH + depth);
        }
        if(type != null) {
            command.add(PARAMETER_TYPE_ONLY + type);
        }
        if(name != null) {
            command.add(name);
        }

        ProcessContext answer = execute(withJ2V8, NPM_LIST_SCRIPT_PATH, command, "Failed to List Packages");
        return answer;
    }

    @Override
    public ProcessContext installPackage(boolean withJ2V8, String name, String version)
        throws ExternalProcessException
    {
        if(!active) {
            throw new ExternalProcessException("Service is not active");
        }

        checkNodeModulesFolder();

        List<String> command = createProcessCommand(withJ2V8, PROCESS_INSTALL);
        if(name == null || name.isEmpty()) {
            throw new ExternalProcessException("Package Name must be provided to install it");
        }
        String packageName = name;
        if(version != null) {
            packageName += "@" + version;
        }
        command.add(packageName);

        ProcessContext answer = execute(withJ2V8, NPM_INSTALL_SCRIPT_PATH, command, "Failed to Install Packages");
        return answer;
    }

    @Override
    public ProcessContext removePackage(boolean withJ2V8, String name, String version)
        throws ExternalProcessException
    {
        if(!active) {
            throw new ExternalProcessException("Service is not active");
        }

        checkNodeModulesFolder();

        List<String> command = createProcessCommand(withJ2V8, PROCESS_REMOVE);
        if(name == null || name.isEmpty()) {
            throw new ExternalProcessException("Package Name must be provided to install it");
        }
        String packageName = name;
        if(version != null) {
            packageName += "@" + version;
        }
        command.add(packageName);

        ProcessContext answer = execute(withJ2V8, NPM_REMOVE_SCRIPT_PATH, command, "Failed to Remove Packages");
        return answer;
    }

    private List<String> createProcessCommand(boolean withJ2V8, String npmCommand) {
        List<String> answer = null;
        if(withJ2V8) {
            answer = new ArrayList<>();
        } else {
            String processName = processRunner.isWindows() ? PROCESS_NAME_WIN : PROCESS_NAME_UNIX;
            answer = new ArrayList<String>(Arrays.asList(processName, npmCommand, PARAMETER_JSON));
        }
        return answer;
    }

    private ProcessContext execute(boolean withJ2V8, String scriptJcrPath, List<String> command, String failureMessage)
        throws ExternalProcessException
    {
        ProcessContext answer = null;
        if(withJ2V8) {
            if(executor != null) {
                answer = processRunner.executeWithJ2V8(executor, scriptJcrPath, command);
            } else {
                throw new ExternalProcessException("J2V8 Executor is not installed").setCommand(command);
            }
        } else {
            answer = processRunner.execute(command);
        }
        if(answer.getExitCode() < 0) {
            throw new ExternalProcessException("Failed to List Packages").setCommand(command).setProcessContext(answer);
        }
        return answer;

    }

    private void checkNodeModulesFolder() {
        String path = System.getProperty("user.dir");
        log.trace("Check Node Modules Folder, User DIR: '{}'", path);
        File userDirectory = new File(path);
        if(userDirectory.exists()) {
            if(userDirectory.isDirectory()) {
                File nodeModulesDirectory = new File(userDirectory, "node_modules");
                if(nodeModulesDirectory.exists()) {
                    if(nodeModulesDirectory.isDirectory()) {
                        log.trace("node_modules folder found and is directory");
                    } else {
                        log.warn("node_modules exists but it is a file -> check this out and resolve");
                    }
                } else {
                    log.trace("node_modules does not exist -> create");
                    if(!nodeModulesDirectory.mkdirs()) {
                        log.trace("node_modules could not be created -> check this out and resolve (probably a permission issue");
                    }
                }
            } else {
                log.trace("It looks like the user directory is wrong (not a folder)");
            }
        }
    }
}
