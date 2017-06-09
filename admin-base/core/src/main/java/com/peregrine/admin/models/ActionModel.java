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
      "source": "inject"
    },
    "target": {
      "type": "string",
      "source": "inject"
    },
    "command": {
      "type": "string",
      "source": "inject"
    },
    "type": {
      "type": "string",
      "source": "inject"
    },
    "icon": {
      "type": "string",
      "source": "inject"
    },
    "stateFrom": {
      "type": "string",
      "source": "inject"
    },
    "stateFromDefault": {
      "type": "string",
      "source": "inject"
    }
  },
  "propertyNames": [
    "component",
    "path",
    "title",
    "target",
    "command",
    "type",
    "icon",
    "stateFrom",
    "stateFromDefault"
  ],
  "modelName": "Action",
  "package": "com.peregrine.admin.models",
  "componentPath": "admin/components/action",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "admin/components/action",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class ActionModel extends AbstractComponent {

    public ActionModel(Resource r) { super(r); }

    //GEN[:INJECT
    /* {"type":"string","source":"inject"} */
@Inject
private String title;

/* {"type":"string","source":"inject"} */
@Inject
private String target;

/* {"type":"string","source":"inject"} */
@Inject
private String command;

/* {"type":"string","source":"inject"} */
@Inject
private String type;

/* {"type":"string","source":"inject"} */
@Inject
private String icon;

/* {"type":"string","source":"inject"} */
@Inject
private String stateFrom;

/* {"type":"string","source":"inject"} */
@Inject
private String stateFromDefault;


//GEN]

    //GEN[:GETTERS
    /* {"type":"string","source":"inject"} */
public String getTitle() {
return title;
}

/* {"type":"string","source":"inject"} */
public String getTarget() {
return target;
}

/* {"type":"string","source":"inject"} */
public String getCommand() {
return command;
}

/* {"type":"string","source":"inject"} */
public String getType() {
return type;
}

/* {"type":"string","source":"inject"} */
public String getIcon() {
return icon;
}

/* {"type":"string","source":"inject"} */
public String getStateFrom() {
return stateFrom;
}

/* {"type":"string","source":"inject"} */
public String getStateFromDefault() {
return stateFromDefault;
}


//GEN]

}
