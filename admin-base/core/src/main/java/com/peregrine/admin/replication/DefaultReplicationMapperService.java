package com.peregrine.admin.replication;

import com.peregrine.commons.util.PerUtil;
import com.peregrine.commons.util.PerUtil.ResourceChecker;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.splitIntoParameterMap;

@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = { DefaultReplicationMapper.class, Replication.class },
    immediate = true
)
@Designate(ocd = DefaultReplicationMapperService.Configuration.class, factory = false)
public class DefaultReplicationMapperService
    implements DefaultReplicationMapper
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ObjectClassDefinition(name = "Peregrine: Default Replication Mapper Service", description = "Provides a mapping between the Path and the Default Replication Service(s)")
    @interface Configuration {
        @AttributeDefinition(name = "Default", description = "Default Mapping Configuration (used if none path covers it). Format: <replication name>[:(<parameter name>=<parameter value>|)*]", required = true) String defaultMapping();

        @AttributeDefinition(name = "Path Mapping", description = "Path Based Mapping Configurations. Format: <replication name>:path=<starting path>[(|<parameter name>=<parameter value>)*]", required = false) String[] pathMapping();
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
    void activate(BundleContext context, Configuration configuration) {
        setup(context, configuration);
    }

    @Modified
    @SuppressWarnings("unused")
    void modified(BundleContext context, Configuration configuration) {
        setup(context, configuration);
    }

    private DefaultReplicationConfig defaultMapping;
    private List<DefaultReplicationConfig> pathMapping = new ArrayList<>();

    private void setup(BundleContext context, final Configuration configuration) {
        logger.trace("Default Mapping: '{}'", configuration.defaultMapping());
        Map<String, Map<String, String>> temp = splitIntoParameterMap(new String[] {configuration.defaultMapping()}, ":", "\\|", "=");
        logger.trace("Mapped Default Mapping: '{}'", temp);
        if(temp.keySet().isEmpty()) { throw new IllegalArgumentException("Default Mapping was not provided but is required"); }
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
    public String getName() {
        return "defaultMapper";
    }

    @Override
    public List<Resource> replicate(Resource source, boolean deep) throws ReplicationException {
        logger.trace("Starting Resource: '{}'", source.getPath());
        List<Resource> referenceList = referenceLister.getReferenceList(source, true);
        logger.trace("Reference List: '{}'", referenceList);
        List<Resource> replicationList = new ArrayList<>();
        ResourceChecker resourceChecker = new ResourceChecker() {
            @Override
            public boolean doAdd(Resource resource) { return true; }

            @Override
            public boolean doAddChildren(Resource resource) { return true; }
        };
        // Need to check this list of they need to be replicated first
        for(Resource resource: referenceList) {
            if(resourceChecker.doAdd(resource)) {
                replicationList.add(resource);
            }
        }
        // This only returns the referenced resources. Now we need to check if there are any JCR Content nodes to be added as well
        for(Resource reference: new ArrayList<Resource>(replicationList)) {
            PerUtil.listMissingResources(reference, replicationList, resourceChecker, false);
        }
        PerUtil.listMissingResources(source, replicationList, resourceChecker, deep);
        return replicate(replicationList);
    }

    @Override
    public List<Resource> deactivate(Resource source) throws ReplicationException {
        return null;
    }

    @Override
    public List<Resource> replicate(List<Resource> resourceList) throws ReplicationException {
        List<Resource> answer = new ArrayList<>();
        Map<DefaultReplicationConfig, List<Resource>> resourceByReplication = new HashMap<>();
        resourceByReplication.put(defaultMapping, new ArrayList<Resource>());
        for(DefaultReplicationConfig config: pathMapping) {
            resourceByReplication.put(config, new ArrayList<Resource>());
        }
        // Now we loop over the all resources, separate them into pods of Replication Services
        for(Resource resource: resourceList) {
            boolean found = false;
            for(DefaultReplicationConfig config: pathMapping) {
                if(config.isHandled(resource)) {
                    resourceByReplication.get(config).add(resource);
                    found = true;
                    break;
                }
            }
            if(!found) {
                resourceByReplication.get(defaultMapping).add(resource);
            }
        }
        for(Entry<DefaultReplicationConfig, List<Resource>> pot: resourceByReplication.entrySet()) {
            Replication replication = replications.get(pot.getKey().getServiceName());
            if(replication == null) { throw new ReplicationException("Could not find replication with name: " + pot.getKey().getServiceName()); }
            List<Resource> replicatedResources = replication.replicate(pot.getValue());
            if(!replicatedResources.isEmpty()) { answer.addAll(replicatedResources); }
        }
        return answer;
    }

    private static class DefaultReplicationConfig {
        private String serviceName;
        private String path;
        private Map<String, String> parameters = new HashMap<>();

        public DefaultReplicationConfig(String serviceName, Map<String, String> parameters) {
            if(isEmpty(serviceName)) { throw new IllegalArgumentException("Replication Service Name cannot be null for mapping"); }
            this.serviceName = serviceName;
            if(parameters != null) { this.parameters.putAll(parameters); }
        }

        public DefaultReplicationConfig(String serviceName, String path, Map<String, String> parameters) {
            this(serviceName, parameters);
            if(isEmpty(path)) { throw new IllegalArgumentException("Replication Path (for non default) Name cannot be null for mapping"); }
            this.path = path;
        }

        public boolean isHandled(Resource resource) {
            return path == null || resource.getPath().startsWith(path);
        }

        public String getServiceName() {
            return serviceName;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "DefaultReplicationConfig{" + "serviceName='" + serviceName + '\'' + ", path='" + path + '\'' + ", parameters=" + parameters + '}';
        }
    }
}
