package com.peregrine.admin.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.Container;
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
    "selectionFrom": {
      "type": "string",
      "source": "inject"
    }
  },
  "propertyNames": [
    "component",
    "path",
    "dataFrom",
    "selectionFrom"
  ],
  "modelName": "Explorer",
  "package": "com.peregrine.admin.models",
  "componentPath": "admin/components/explorer",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "admin/components/explorer",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class ExplorerModel extends Container {

    public ExplorerModel(Resource r) { super(r); }

    //GEN[:INJECT
    /* {"type":"string","source":"inject"} */
@Inject
private String dataFrom;

/* {"type":"string","source":"inject"} */
@Inject
private String selectionFrom;


//GEN]

    //GEN[:GETTERS
    /* {"type":"string","source":"inject"} */
public String getDataFrom() {
return dataFrom;
}

/* {"type":"string","source":"inject"} */
public String getSelectionFrom() {
return selectionFrom;
}


//GEN]

}
