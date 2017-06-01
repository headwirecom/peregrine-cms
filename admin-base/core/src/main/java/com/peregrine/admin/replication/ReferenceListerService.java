package com.peregrine.admin.replication;

import com.peregrine.admin.util.JcrUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.util.JcrUtil.JCR_CONTENT;

/**
 * Created by schaefa on 5/25/17.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    service = ReferenceLister.class,
    immediate = true
)
@Designate(ocd = ReferenceListerService.Configuration.class)
public class ReferenceListerService
    implements ReferenceLister
{
    @ObjectClassDefinition(
        name = "Peregrine: Reference List Provider",
        description = "Provides a list of referenced resources for a given resource"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "ReferencePrefix",
            description = "List of Prefixes to find references in a resource",
            required = true
        )
        String[] referencePrefix() default "/content/";
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private List<String> referencePrefixList = new ArrayList<>();

    @Override
    public List<Resource> getReferenceList(Resource resource, boolean deep) {
        return getReferenceList(resource, deep, null, null);
    }

    @Override
    public List<Resource> getReferenceList(Resource resource, boolean deep, Resource source, Resource target) {
        List<Resource> answer = new ArrayList<>();
        traverse(resource, answer, deep, source, target);
        return answer;
    }

    private void traverse(Resource resource, List<Resource> response, boolean deep, Resource source, Resource target) {
        Resource jcrContent = resource.getChild(JCR_CONTENT);
        if(jcrContent != null) {
            ValueMap properties = jcrContent.getValueMap();
            for(Object item: properties.values()) {
                String value = item + "";
                for(String prefix: referencePrefixList) {
                    if(value.startsWith(prefix)) {
                        String resourcePath = value;
                        log.trace("Found Reference Resource Path: '{}'", resourcePath);
                        Resource child = null;
                        if(resourcePath.startsWith("/")) {
                            child = resource.getResourceResolver().getResource(value);
                        } else {
                            child = resource.getChild(resourcePath);
                        }
                        if(child != null) {
                            // Check if the resource is not already listed in there
                            if(response.contains(child)) {
                                log.info("Resource is already in the list: '{}'", child);
                            } else {
                                if(source  != null && target != null) {
                                    JcrUtil.listMissingParents(child, response, source, target);
                                }
                                log.trace("Found Reference Resource: '{}'", child);
                                response.add(child);
                                if(deep) {
                                    traverse(child, response, deep, source, target);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }

    private void setup(Configuration configuration) {
        String[] prefixes = configuration.referencePrefix();
        referencePrefixList = new ArrayList<>();
        for(String prefix: prefixes) {
            if(prefix != null && !prefix.isEmpty()) {
                log.debug("Add Reference Prefix: '{}'", prefix);
                referencePrefixList.add(prefix);
            }
        }
    }
}
