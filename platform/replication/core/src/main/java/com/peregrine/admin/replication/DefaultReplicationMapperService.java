package com.peregrine.admin.replication;

import com.peregrine.commons.ResourceUtils;
import com.peregrine.replication.ReferenceLister;
import com.peregrine.replication.Replication;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.peregrine.commons.util.PerUtil.AddAllResourceChecker;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.listMissingResources;
import static com.peregrine.commons.util.PerUtil.splitIntoParameterMap;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This class provides the implementation of the Default Replication Mapper
 * service for path based mapping. It has two settings:
 * - Default Replication: any non-matching replication will be replicated with that Replication Service
 * - Path Mapping: A Replication Service is mapped to a path and its sub folders.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = DefaultReplicationMapper.class,
    immediate = true
)
@Designate(ocd = DefaultReplicationMapperService.Configuration.class, factory = true)
public class DefaultReplicationMapperService
    extends AbstractionReplicationService
    implements DefaultReplicationMapper
{
    public static final String NO_DEFAULT_MAPPING = "Default Mapping was not provided but is required";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ObjectClassDefinition(
        name = "Peregrine: Default Replication Mapper Service",
        description = "Provides a mapping between the Path and the Default Replication Service(s)"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Name",
            description = "Name of the Default Replication Service (for the UI at least one service with 'defaultRepl' is required)",
            defaultValue = "defaultRepl",
            required = true
        )
        String name();
        @AttributeDefinition(
            name = "Default",
            description = "Default Mapping Configuration (used if none path covers it). Format: <replication name>[:(<parameter name>=<parameter value>|)*]",
            required = true
        )
        String defaultMapping();
        @AttributeDefinition(
            name = "Description",
            description = "Description of this Replication Service",
            required = true
        )
        String description();
        @AttributeDefinition(
            name = "Path Mapping",
            description = "Path Based Mapping Configurations. Format: <replication name>:path=<starting path>[(|<parameter name>=<parameter value>)*]",
            required = false
        )
        String[] pathMapping();
    }

    @Reference
    @SuppressWarnings("unused")
    private ReferenceLister referenceLister;

    private Map<String, Replication> replications = new HashMap<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindReplication(Replication replication) {
        logger.trace("Bind DRMS Replication: '{}'", replication.getName());
        String replicationName = replication.getName();
        if(replicationName != null && !replicationName.isEmpty()) {
            replications.put(replicationName, replication);
        } else {
            logger.error("Replication: '{}' does not provide an operation name -> binding is ignored", replication);
        }
    }

    @SuppressWarnings("unused")
    public void unbindReplication(Replication replication) {
        String replicationName = replication.getName();
        if(replications.containsKey(replicationName)) {
            replications.remove(replicationName);
        } else {
            logger.error("Replication: '{}' is not register with operation name: '{}' -> unbinding is ignored", replication, replicationName);
        }
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) {
        setup(configuration);
    }

    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) {
        setup(configuration);
    }

    private DefaultReplicationConfig defaultMapping;
    private List<DefaultReplicationConfig> pathMapping = new ArrayList<>();

    private void setup(final Configuration configuration) {
        init(configuration.name(), configuration.description());
        // Register this service as Replication instance
        logger.trace("Default Mapping: '{}'", configuration.defaultMapping());
        Map<String, Map<String, String>> temp = splitIntoParameterMap(new String[] {configuration.defaultMapping()}, ":", "\\|", "=");
        logger.trace("Mapped Default Mapping: '{}'", temp);
        if(temp.isEmpty()) {
            throw new IllegalArgumentException(NO_DEFAULT_MAPPING);
        }
        Entry<String, Map<String, String>> entry = temp.entrySet().iterator().next();
        defaultMapping = new DefaultReplicationConfig(entry.getKey(), entry.getValue());
        logger.trace("Final Default Mapping: '{}'", defaultMapping);
        String[] pathMappings = configuration.pathMapping();
        logger.trace("Path Mapping: '{}'", Arrays.asList(pathMappings));
        pathMapping.clear();
        if(pathMappings.length > 0) {
            temp = splitIntoParameterMap(pathMappings, ":", "\\|", "=");
            logger.trace("Mapped Path Mapping: '{}'", temp);
            // Check that each mapping has a Path (otherwise it is futile)
            for(Entry<String, Map<String, String>> tempEntry: temp.entrySet()) {
                Map<String, String> parameters = tempEntry.getValue();
                String path = parameters.remove("path");
                pathMapping.add(
                    new DefaultReplicationConfig(tempEntry.getKey(), path, parameters)
                );
            }
            logger.trace("Final Path Mapping: '{}'", pathMapping);
        }
    }

    @Override
    public List<Resource> replicate(Resource source, boolean deep) throws ReplicationException {
        logger.trace("Starting Resource: '{}'", source.getPath());
        final List<Resource> referenceList = referenceLister.getReferenceList(true, source, deep);
//        logger.trace("Reference List: '{}'", referenceList);
        final List<Resource> replicationList = listMissingResources(source, new ArrayList<>(), new AddAllResourceChecker(), deep);
        replicationList.add(0, source);
        replicationList.addAll(0, referenceList);
        ResourceUtils.removeDuplicates(replicationList);
        return replicate(replicationList);
    }

    @Override
    public List<Resource> deactivate(final Resource source) throws ReplicationException {
        return getDefaultReplicationService().deactivate(source);
    }

    @Override
    public List<Resource> replicate(Collection<Resource> resourceList) throws ReplicationException {
        List<Resource> answer = new ArrayList<>();
        Map<DefaultReplicationConfig, List<Resource>> resourceByReplication = new HashMap<>();
        resourceByReplication.put(defaultMapping, new ArrayList<>());
        for(DefaultReplicationConfig config: pathMapping) {
            resourceByReplication.put(config, new ArrayList<>());
        }
        // Now we loop over the all resources, separate them into pods of Replication Services
        for (final Resource resource : resourceList) {
            getReplications(resource).stream()
                    .map(resourceByReplication::get)
                    .forEach(l -> l.add(resource));
        }

        for (Entry<DefaultReplicationConfig, List<Resource>> pot: resourceByReplication.entrySet()) {
            final String serviceName = pot.getKey().getServiceName();
            final Replication replication = replications.get(serviceName);
            if (isNull(replication)) {
                throw new ReplicationException("Could not find replication with name: " + serviceName);
            }

            final String replicationName = replication.getName();
            final List<Resource> resources = pot.getValue();
            logger.trace("Replicate with Replication: '{}' these resources: '{}'", replicationName, resources);
            logger.trace("DRH Replication: '{}', Replicates: '{}'", replicationName, resources);
            answer.addAll(replication.replicate(resources));
        }

        return answer;
    }

    private List<DefaultReplicationConfig> getReplications(final Resource resource) {
        final List<DefaultReplicationConfig> result = new LinkedList<>();
        boolean handled = false;
        for (final DefaultReplicationConfig config: pathMapping) {
            if (config.isHandled(resource)) {
                logger.trace("Replicate Resource: '{}' using DRC: '{}'", resource.getPath(), config);
                result.add(config);
                // Resource is handled if the service name here is the same as for the default
                if (!handled) {
                    handled = !config.serviceName.equals(defaultMapping.serviceName);
                }
            }
        }

        if (!handled) {
            // Resource was not added to default mapping so add it here
            logger.trace("Replicate Resource: '{}' using default DRC: '{}'", resource.getPath(), defaultMapping);
            result.add(defaultMapping);
        }

        return result;
    }

    private Replication getDefaultReplicationService() {
        return this.replications.get(this.defaultMapping.getServiceName());
    }

    public String storeFile(final Resource parent, final String name, final String content)
            throws ReplicationException {
        return storeFile(parent, r -> {
            try {
                return r.storeFile(parent, name, content);
            } catch (final ReplicationException e) {
                return null;
            }
        });
    }

    public String storeFile(final Resource parent, final String name, final byte[] content)
            throws ReplicationException {
        return storeFile(parent, r -> {
            try {
                return r.storeFile(parent, name, content);
            } catch (final ReplicationException e) {
                return null;
            }
        });
    }

    public String storeFile(final Resource parent, final Function<Replication, String> fileConsumer)
            throws ReplicationException {
        final StringBuilder answer = new StringBuilder("[");
        for (final DefaultReplicationConfig config: getReplications(parent)) {
            final String serviceName = config.getServiceName();
            final Replication replication = replications.get(serviceName);
            if (isNull(replication)) {
                throw new ReplicationException("Could not find replication with name: " + serviceName);
            }

            final String location = fileConsumer.apply(replication);
            if (isNotBlank(location)) {
                answer.append(location).append(",");
            }
        }

        return answer.append("]").toString();
    }

    /**
     * This class contains the configuration properties parsed from the Service Configurations
     */
    static class DefaultReplicationConfig {
        public static final String REPLICATION_SERVICE_NAME_CANNOT_BE_NULL = "Replication Service Name cannot be null for mapping";
        public static final String REPLICATION_PATH_FOR_NON_DEFAULT_NAME_CANNOT_BE_NULL = "Replication Path (for non default) Name cannot be null for mapping";
        private String serviceName;
        private String path;
        private Map<String, String> parameters = new HashMap<>();

        /** Configuration for the Default Replication **/
        public DefaultReplicationConfig(String serviceName, Map<String, String> parameters) {
            if(isEmpty(serviceName)) { throw new IllegalArgumentException(REPLICATION_SERVICE_NAME_CANNOT_BE_NULL); }
            this.serviceName = serviceName;
            if(parameters != null) {
                this.parameters.putAll(parameters);
            }
        }

        /** Configuration for a single Path Mapping **/
        public DefaultReplicationConfig(String serviceName, String path, Map<String, String> parameters) {
            this(serviceName, parameters);
            if(isEmpty(path)) {
                throw new IllegalArgumentException(REPLICATION_PATH_FOR_NON_DEFAULT_NAME_CANNOT_BE_NULL);
            }
            this.path = path;
        }

        /**
         * Checks if this configuration applies to the given Resource
         * @param resource Resource to be checked if it applies
         * @return True if this resource should be handled with the given replication
         */
        public boolean isHandled(Resource resource) {
            // If the config path does not end in a slash we must make sure that either the resource
            // path is the same or that the next character is a slash otherwise folders starting the
            // same will match but they should not (/test/one should not match /test/one-1)
            boolean answer = false;
            String resourcePath = resource.getPath();
            if(path != null && !path.endsWith("/")) {
                if (path.contains("_tenant_")) {
                    String tenant = resourcePath.split("/")[2];
                    path = path.replace("_tenant_", tenant);
                }
                if(resourcePath.startsWith(path)) {
                    if(resourcePath.equals(path)) {
                        answer = true;
                    } else {
                        char next = resourcePath.charAt(path.length());
                        answer = (next == '/');
                    }
                } else {
                    answer = false;
                }
            } else {
                answer = (path == null || resource.getPath().startsWith(path));
            }
            return answer;
        }

        /** @return the Replication Service Name **/
        public String getServiceName() {
            return serviceName;
        }

        /** @return The Service Parameters **/
        public Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "DefaultReplicationConfig{" + "serviceName='" + serviceName + '\'' + ", path='" + path + '\'' + ", parameters=" + parameters + '}';
        }
    }

}
