package com.peregrine.nodejs.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.nodejs.process.ExternalProcessException;
import com.peregrine.nodejs.process.ProcessContext;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.getProperties;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.loginService;
import static com.peregrine.nodejs.servlet.SlingNodeConstants.LIST_TYPE_ALL;
import static com.peregrine.nodejs.util.NodeConstants.NODE_JS_SUB_SERVICE_NAME;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.CHANGES;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = ResourceChangeListener.class,
    configurationPolicy = ConfigurationPolicy.IGNORE,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "NPM Package Configuration Listener",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        PATHS + EQUALS + "glob:/config/nodejs/**",
        CHANGES + EQUALS + "ADDED",
        CHANGES + EQUALS + "CHANGED",
        CHANGES + EQUALS + "REMOVED"
    }
)
public class NpmPackageConfigurationEventHandlerService
    implements ResourceChangeListener
{
    public static final String NPM_PACKAGE_CONFIG_PRIMARY_TYPE = "per:NpmPackageConfig";
    public static final String VERSION_LATEST = "latest";
    public static final String DEPENDENCIES = "dependencies";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    @SuppressWarnings("unused")
    private NpmExternalProcess npmExternalProcess;

    @Override
    public void onChange(List<ResourceChange> changes) {
        if(changes != null) {
            for(ResourceChange change: changes) {
                log.trace("Resource Change: '{}'", change);
                // Obtain the resource, check that it is a NPM Package Configuration
                ResourceResolver resourceResolver = null;
                try {
                    resourceResolver = loginService(resourceResolverFactory, NODE_JS_SUB_SERVICE_NAME);
                    Resource resource = getResource(resourceResolver, change.getPath());
                    String primaryType = getPrimaryType(resource);
                    if(NPM_PACKAGE_CONFIG_PRIMARY_TYPE.equals(primaryType)) {
                        ValueMap properties = getProperties(resource, false);
                        if(properties != null) {
//                            ProcessContext result = null;
//                            try {
//                                result = npmExternalProcess.listPackages(false, null, 1, null, 1);
//                            } catch(ExternalProcessException e) {
//                                log.error("Failed to list all installed Packages", e);
//                            }
//                            if(result != null) {
//                                String json = result.getOutput();
//                                log.trace("JSon from List Packages: '{}'", json);
//                                if(json == null || json.isEmpty()) {
//                                    // In case there is nothing installed in the local node_modules the list packages fails and returns null
//                                    json = "{\"dependencies\":{}}";
//                                }
                                // Loop through all Properties and handle all non-jcr/non-sling properties
                                // which are considered a package
                                for(String packageName: properties.keySet()) {
                                    // Filter all properties with a color like jcr: or sling:
                                    //AS TODO: Can a Node Package have a colon in its name? If so we need to excluded specific prefixes
                                    if(packageName.indexOf(':') < 0) {
                                        String packageVersion = properties.get(packageName, VERSION_LATEST);
                                        if(packageVersion.isEmpty() || packageVersion.equalsIgnoreCase(VERSION_LATEST)) {
                                            packageVersion = null;
                                        }
//                                        // Get the package name, check if they are already installed and if not install them
//                                        Map npmPackage = getPackageFromList(json, packageName);
//                                        if(npmPackage == null) {
                                            // Listing failed or No package found -> install it
                                            try {
                                                npmExternalProcess.installPackage(false, packageName, packageVersion);
                                            } catch(ExternalProcessException e) {
                                                log.error("Failed to install package: " + packageName, e);
                                            }
//                                        }
                                    }
                                }
//                            }
                        }
                    }
                } catch(LoginException e) {
                    log.error("Could not obtain Node JS Service Resource Resolver", e);
                } finally {
                    if(resourceResolver != null) { resourceResolver.close(); }
                }
            }
        }
    }

    private Map getPackageFromList(String json, String name) {
        Map answer = null;
        Map jsonMap = null;
        try {
            jsonMap = convertToMap(json);
            log.trace("NPM List report: '{}'", jsonMap);
            if(jsonMap.containsKey(DEPENDENCIES)) {
                Map dependencies = (Map) jsonMap.get(DEPENDENCIES);
                if(dependencies.containsKey(name)) {
                    answer = (Map) dependencies.get(name);
                }
            }
        } catch(IOException e) {
            log.warn("Failed to convert JSon: '" + json + "' into map", e);
        }
        return answer;
    }
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static Map convertToMap(String json) throws IOException {
        Map answer = new LinkedHashMap();
        if(json != null) {
            answer = JSON_MAPPER.readValue(json, LinkedHashMap.class);
        }
        return answer;
    }
}
