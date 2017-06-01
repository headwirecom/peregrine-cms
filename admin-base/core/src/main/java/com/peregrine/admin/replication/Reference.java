package com.peregrine.admin.replication;

import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by schaefa on 5/25/17.
 */
public class Reference {
    private Resource source;
    private List<Reference> references = new ArrayList<>();

    public Reference(Resource source) {
        this.source = source;
    }

    public Resource getSource() {
        return source;
    }

    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public Reference addReference(Resource resource) {
        Reference answer = resource != null ? new Reference(resource) : null;
        if(answer != null) {
            references.add(answer);
        }
        return answer;
    }
}
