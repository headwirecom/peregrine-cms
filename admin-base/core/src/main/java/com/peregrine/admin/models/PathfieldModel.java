package com.peregrine.admin.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;

/*
    //GEN[:DATA
    {
  "type": "object",
  "properties": {
    "component": {
      "type": "string",
      "source": "ignore"
    },
    "path": {
      "type": "string",
      "source": "ignore"
    },
    "dataFrom": {
      "type": "string",
      "source": "inject"
    },
    "dataDefault": {
      "type": "string",
      "source": "inject"
    }
  },
  "propertyNames": [
    "component",
    "path",
    "dataFrom",
    "dataDefault"
  ],
  "modelName": "Pathfield",
  "package": "com.peregrine.admin.models",
  "componentPath": "admin/components/pathfield",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "admin/components/pathfield",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class PathfieldModel extends AbstractComponent {

    public PathfieldModel(Resource r) { super(r); }

    //GEN[:INJECT
    /* {"type":"string","source":"inject"} */
@Inject
private String dataFrom;

/* {"type":"string","source":"inject"} */
@Inject
private String dataDefault;


//GEN]

    //GEN[:GETTERS
    /* {"type":"string","source":"inject"} */
public String getDataFrom() {
return dataFrom;
}

/* {"type":"string","source":"inject"} */
public String getDataDefault() {
return dataDefault;
}


//GEN]

}
