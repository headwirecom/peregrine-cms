package com.peregrine.nodetypes.models;

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = Resource.class,
       resourceType = {NT_UNSTRUCTURED},
       adapters = IComponent.class)
@Exporter(name = JACKSON,
        extensions = JSON)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class NtUnstructuredModel
    extends Container {

    public NtUnstructuredModel(Resource r) {
        super(r);
    }

    @JsonIgnore(value = false)
    public String getName() {
        return getResource().getName();
    }

    @JsonAnyGetter
    public Map valueMap() {
        return getResource().getValueMap();
    }

}
