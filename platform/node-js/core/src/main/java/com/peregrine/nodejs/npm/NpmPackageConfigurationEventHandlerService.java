package com.peregrine.nodejs.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.nodejs.process.ExternalProcessException;
import com.peregrine.nodejs.util.NodeConstants;
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
import static com.peregrine.nodejs.util.NodeConstants.NODE_JS_SUB_SERVICE_NAME;
import static com.peregrine.nodejs.util.NodeConstants.NPM_PACKAGE_CONFIG_PATH;
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
        PATHS + EQUALS + "glob:" + NPM_PACKAGE_CONFIG_PATH + "/**",
        CHANGES + EQUALS + "ADDED",
        CHANGES + EQUALS + "CHANGED",
        CHANGES + EQUALS + "REMOVED"
    }
)
public class NpmPackageConfigurationEventHandlerService
    implements ResourceChangeListener
{

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
                    if(NodeConstants.NPM_PACKAGE_CONFIG_PRIMARY_TYPE.equals(primaryType)) {
                        ValueMap properties = getProperties(resource, false);
                        if(properties != null) {
                            // Package List of NPM is not reliable and therefore not used here
                            // Loop through all Properties and handle all non-jcr/non-sling properties
                            // which are considered a package
                            for(String packageName: properties.keySet()) {
                                // Filter all properties with a color like jcr: or sling:
                                //AS TODO: Can a Node Package have a colon in its name? If so we need to excluded specific prefixes
                                if(packageName.indexOf(':') < 0) {
                                    String packageVersion = properties.get(packageName, NodeConstants.VERSION_LATEST);
                                    if(packageVersion.isEmpty() || packageVersion.equalsIgnoreCase(NodeConstants.VERSION_LATEST)) {
                                        packageVersion = null;
                                    }
                                    try {
                                        npmExternalProcess.installPackage(false, packageName, packageVersion);
                                    } catch(ExternalProcessException e) {
                                        log.error("Failed to install package: " + packageName, e);
                                    }
                                }
                            }
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
            if(jsonMap.containsKey(NodeConstants.DEPENDENCIES)) {
                Map dependencies = (Map) jsonMap.get(NodeConstants.DEPENDENCIES);
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
