package com.peregrine.nodejs.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.nodejs.j2v8.ScriptException;
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
        PATHS + EQUALS + "/config/nodejs/",
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
                        ValueMap properties = getProperties(resource);
                        if(properties != null) {
                            // Loop through all Properties and handle all non-jcr/non-sling properties
                            // which are considered a package
                            for(String packageName: properties.keySet()) {
                                // Filter all properties with a color like jcr: or sling:
                                //AS TODO: Can a Node Package have a colon in its name? If so we need to excluded specific prefixes
                                if(packageName.indexOf(':') < 0) {
                                    String packageVersion = properties.get(packageName, VERSION_LATEST);
                                    // Get the package name, check if they are already installed and if not install them
                                    String json = null;
                                    try {
                                        ProcessContext result = npmExternalProcess.listPackages(false, packageName, 1, LIST_TYPE_ALL, 1);
                                        if(result.getExitCode() == 0) {
                                            json = result.getOutput();
                                            Map jsonMap = convertToMap(json);

                                        } else {
                                            log.error("Failed to list package: '{}', error: '{}'", packageName, result.getError());
                                            break;
                                        }
                                    } catch(ExternalProcessException e) {
                                        log.error("Failed to list Package", e);
                                    } catch(IOException e) {
                                        log.error("Failed to Convert into Map: " + json, e);
                                    }
                                }
                            }
                        }
                    }
                } catch(LoginException e) {
//                    throw new ScriptException(COULD_OBTAIN_RESOURCE_RESOLVER, e);
                } finally {
                    if(resourceResolver != null) { resourceResolver.close(); }
                }
            }
        }
    }

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static Map convertToMap(String json) throws IOException {
        Map answer = new LinkedHashMap();
        if(json != null) {
            answer = JSON_MAPPER.readValue(json, LinkedHashMap.class);
        }
        return answer;
    }

//    @Override
//    public void handleEvent(Event event) {
//        final String topic = event.getTopic();
//        String kind;
//        if(DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED.equals(topic)) {
//            // Forward Agent Event
//            // Check the expected properties
//            if(!checkEventProperties(event, DISTRIBUTION_TYPE_ADD, DISTRIBUTION_COMPONENT_KIND_AGENT, EVENT_TOPICS + EQUALS + DistributionEventTopics.AGENT_PACKAGE_DISTRIBUTED)) {
//                // Ignore -> Done
//                logEvent("Received unexpected Agent Event", event);
//                return;
//            }
//            kind = AGENT;
//
//        } else
//        if(DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED.equals(topic)) {
//            // Forward Agent Event
//            // Check the expected properties
//            if(!checkEventProperties(event, DISTRIBUTION_TYPE_ADD, DISTRIBUTION_COMPONENT_KIND_IMPORTER, EVENT_TOPICS + EQUALS + DistributionEventTopics.IMPORTER_PACKAGE_IMPORTED)) {
//                // Ignore -> Done
//                logEvent("Received unexpected Importer Event", event);
//                return;
//            }
//            kind = IMPORTER;
//        } else {
//            log.trace("Received unhandled event: '{}'", event);
//            return;
//        }
//        Object value = event.getProperty(DistributionEventProperties.DISTRIBUTION_PATHS);
//        if(value instanceof String[]) {
//            String[] paths = (String[]) value;
//            for(String path: paths) {
//                log.trace("Set Replication Properties on: '{}'", path);
//                try {
//                    setReplicationProperties(path, kind);
//                } catch(Throwable t) {
//                    log.warn("Set Replication Properties failed", t);
//                }
//            }
//        }
//    }

//    private void setReplicationProperties(String path, String kind) {
//        ResourceResolver resourceResolver = null;
//        try {
//            resourceResolver = loginService(resourceResolverFactory, DISTRIBUTION_SUB_SERVICE);
//            log.trace("Resource Resolver: '{}'", resourceResolver);
//            Resource resource = getResource(resourceResolver, path);
//            log.trace("Resource for Path: '{}': '{}'", path, resource);
//            updateReplicationProperties(resource, kind + "://" + path, null);
//            resourceResolver.commit();
//        } catch(LoginException e) {
//            log.warn("Failed to set Replication Properties on Resource: " + path + " due to login issue", e);
//        } catch(PersistenceException e) {
//            log.warn("Failed to set Replication Properties on Resource: " + path + " due to persisting issue", e);
//        } finally {
//            if(resourceResolver != null) {
//                try {
//                    resourceResolver.close();
//                } catch(Exception e) {
//                    // ignore
//                }
//            }
//        }
//    }
//
//    private void logEvent(String message, Event event) {
//        String[] propertyNames = event.getPropertyNames();
//        Map<String, String> properties = new HashMap<>();
//        for(String name: propertyNames) {
////            if(DISTRIBUTION_PATHS.equals(name)) {
////                properties.put(name, Arrays.asList((String[])event.getProperty(name)) + "");
////            } else {
//                properties.put(name, event.getProperty(name) + "");
////            }
//        }
//        log.warn(message + ", Topic: '{}', Properties: '{}'", event.getTopic(), properties);
//    }
//
//    private boolean checkEventProperties(Event event, String ... expectedPairs) {
//        boolean answer = true;
//        for(String pair: expectedPairs) {
//            String[] tokens = pair.split(EQUALS);
//            if(tokens.length == 2) {
//                Object value = event.getProperty(tokens[0]);
//                if(value == null) {
//                    log.trace("Event Property: '{}' is not found", tokens[0]);
//                    answer = false;
//                } else {
//                    String text = value + "";
//                    answer = text.equals(tokens[1]);
//                    log.trace("Event Property: '{}' does not match. Found: '{}', Expected: '{}'", tokens[0], text, tokens[1]);
//                }
//            } else {
//                log.trace("Pair: '{}' is not of 'name=value' format", pair);
//            }
//        }
//        return answer;
//    }
}
