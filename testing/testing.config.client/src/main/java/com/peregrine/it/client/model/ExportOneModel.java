package com.peregrine.it.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;

/**
 * Created by rr on 4/18/2017.
 */
@Model(adaptables = Resource.class,
       resourceType = "it/objects/export/one",
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
       adapters = IComponent.class)
@Exporter(name = JACKSON,
          extensions = JSON,
          selector = "export")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ExportOneModel
    extends AbstractComponent
{

    private final Logger log = LoggerFactory.getLogger(getClass());

    public ExportOneModel(Resource resource) {
        super(resource);
    }

    @Override
    @JsonIgnore
    public String getPath() {
        log.info("Export One Path: '{}'", super.getPath());
        return super.getPath();
    }

    @Override
    @JsonIgnore
    public String getComponent() {
        log.info("Export One Component: '{}'", super.getComponent());
        return super.getComponent();
    }

    @Inject
    @Named(".")
    public List<IComponent> children;

    @JsonValue
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<IComponent> getChildren() {
        log.info("Export One Children: '{}'", children);
        //        ArrayList<IComponent> answer = new ArrayList<>();
        //        addChildren(answer, children);
        return children;
    }

    private void addChildren(ArrayList<IComponent> answer, List<IComponent> children) {
        for (IComponent node: children) {
            answer.add(node);
            addChildren(answer, node.getChildren());
        }

    }
}
