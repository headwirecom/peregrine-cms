package com.peregrine.nodejs.npm;

import com.peregrine.nodejs.util.NodeConstants;
import com.peregrine.process.ExternalProcessException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.getProperties;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.loginService;
import static com.peregrine.nodejs.util.NodeConstants.NODE_JS_SUB_SERVICE_NAME;
import static com.peregrine.nodejs.util.NodeConstants.NPM_PACKAGE_CONFIG_PATH;
import static com.peregrine.nodejs.util.NodeConstants.PACKAGES_PROPERTY_NAME;
import static com.peregrine.nodejs.util.NodeConstants.VERSION_LATEST;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.CHANGES;
import static org.apache.sling.api.resource.observation.ResourceChangeListener.PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = ResourceChangeListener.class,
    configurationPolicy = ConfigurationPolicy.IGNORE,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "NPM Package Configuration Listener",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        PATHS + EQUAL + "glob:" + NPM_PACKAGE_CONFIG_PATH + "/**",
        CHANGES + EQUAL + "ADDED",
        CHANGES + EQUAL + "CHANGED",
        CHANGES + EQUAL + "REMOVED"
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
                            Object temp = properties.get(PACKAGES_PROPERTY_NAME);
                            List<String> packages;
                            log.trace("NPM Packages: '{}', class: '{}'", temp, temp == null ? "null" : temp.getClass());
                            if(temp instanceof String[]) {
                                packages = new ArrayList<String>(Arrays.asList((String[]) temp));
                            } else {
                                log.error("There is no '{}' property in node: '{}' or is of an unexpected type -> configuration ignored", PACKAGES_PROPERTY_NAME, resource.getPath());
                                continue;
                            }
                            log.trace("NPM Packages: '{}'", packages);
                            for(String packageName: packages) {
                                if(packageName != null && !packageName.isEmpty()) {
                                    String packageVersion = VERSION_LATEST;
                                    int index = packageName.indexOf('@');
                                    if(index == 0) {
                                        log.error("Package has no name: '{}' -> ignored", packageName);
                                        continue;
                                    } else if(index == packageName.length() - 1) {
                                        log.error("Package has @ but no version: '{}' -> use latest", packageName);
                                        packageName = packageName.substring(0, index);
                                    } else if(index > 0) {
                                        // Package Version is provided
                                        packageVersion = packageName.substring(index + 1);
                                        packageName = packageName.substring(0, index);
                                    }
                                    try {
                                        log.info("Install NPM Package Name: '{}' and Version: '{}'", packageName, packageVersion);
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
                } catch(RuntimeException e) {
                    log.error("Unexpected Exception while installing resource: " + change.getPath(), e);
                } finally {
                    if(resourceResolver != null) { resourceResolver.close(); }
                }
            }
        }
    }
}
