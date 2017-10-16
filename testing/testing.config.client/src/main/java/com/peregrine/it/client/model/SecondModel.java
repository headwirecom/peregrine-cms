package com.peregrine.it.client.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;

/**
 * Created by rr on 4/18/2017.
 */
@Model(adaptables = Resource.class,
       resourceType = "example/objects/collection",
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
       adapters = IComponent.class)
@Exporter(name = JACKSON,
          extensions = JSON,
          selector = "export")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class SecondModel
    extends AbstractComponent {

    public SecondModel(Resource resource) {
        super(resource);
    }


    @Inject private String breadcrumb;

    public String getBreadcrumb() {
        ArrayList<Resource> hierarchy = getHierarchy();

        StringBuilder sb = new StringBuilder();
        for(int i = hierarchy.size() - 1; i >= 0; i--) {
            sb.append(hierarchy.get(i).getValueMap().get("breadcrumb", String.class));
            if(i > 0) {
                sb.append(" / ");
            }
        }
        return sb.toString();
    }

    public String getPath() {

        ArrayList<Resource> hierarchy = getHierarchy();

        StringBuilder sb = new StringBuilder();
        for(int i = hierarchy.size() - 1; i >= 0; i--) {
            sb.append("/");
            sb.append(hierarchy.get(i).getName());
        }
        return sb.toString();

    }

    private ArrayList<Resource> getHierarchy() {
        ArrayList<Resource> hierarchy = new ArrayList<>();
        Resource resource = getResource();
        while(resource.getResourceType().equals("weedguide/objects/section")) {
            hierarchy.add(resource);
            resource = resource.getParent();
        }
        return hierarchy;
    }

    @Inject
    @Named(".")
    private List<IComponent> children;

    @Inject private List<IComponent> additionalRequestParameters;

    @Inject private IComponent queryParams;

    public List<IComponent> getAdditionalRequestParameters() {
        return additionalRequestParameters;
    }

    public IComponent getQueryParams() {
        return queryParams;
    }

    @JsonAnyGetter
    public Map valueMap() {
        HashMap<String, Object> answer = new HashMap<>();
        Map values = getResource().getValueMap();
        for(Object key : values.keySet()) {
            String k = (String) key;
            if((k.indexOf(":") < 0 && !k.equals("breadcrumb"))) {
                String value = (String) values.get(key);
                if(value != null && value.startsWith("/content/objects")) {
                    Resource val = getResource().getResourceResolver().getResource(value);
                    if(val != null) {
                        value = val.getValueMap().get("jcr:title", String.class);
                    }
                }
                answer.put(k, value);
            }
        }

        return answer;
    }

    @Override
    @JsonIgnore(value = false)
    public List<IComponent> getChildren() {
        return children;
    }

}
