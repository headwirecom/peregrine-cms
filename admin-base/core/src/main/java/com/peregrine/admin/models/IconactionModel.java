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
    "title": {
      "type": "string",
      "source": "inject",
      "sourceName": "jcr:title"
    },
    "description": {
      "type": "string",
      "source": "inject",
      "sourceName": "jcr:description"
    },
    "action": {
      "type": "string",
      "source": "inject"
    }
  },
  "propertyNames": [
    "component",
    "path",
    "title",
    "description",
    "action"
  ],
  "modelName": "Iconaction",
  "package": "com.peregrine.admin.models",
  "componentPath": "admin/components/iconaction",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "admin/components/iconaction",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class IconactionModel extends AbstractComponent {

    public IconactionModel(Resource r) { super(r); }

    //GEN[:INJECT
    /* {"type":"string","source":"inject","sourceName":"jcr:title"} */
@Inject
@Named(value ="jcr:title")
private String title;

/* {"type":"string","source":"inject","sourceName":"jcr:description"} */
@Inject
@Named(value ="jcr:description")
private String description;

/* {"type":"string","source":"inject"} */
@Inject
private String action;


//GEN]

    //GEN[:GETTERS
    /* {"type":"string","source":"inject","sourceName":"jcr:title"} */
public String getTitle() {
return title;
}

/* {"type":"string","source":"inject","sourceName":"jcr:description"} */
public String getDescription() {
return description;
}

/* {"type":"string","source":"inject"} */
public String getAction() {
return action;
}


//GEN]

}
