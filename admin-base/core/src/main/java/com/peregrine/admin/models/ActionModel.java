package com.peregrine.admin.models;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import static com.peregrine.admin.util.AdminConstants.ACTION_COMPONENT_PATH;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

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
        resourceType = ACTION_COMPONENT_PATH,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(name = JACKSON,
          extensions = JSON)
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

    /* {"type":"string","source":"inject"} */
    @Inject
    private String classes;

    /* {"type":"string","source":"inject"} */
    public String getClasses() {
      return classes;
      }

      /* {"type":"string","source":"inject"} */
    @Inject
    private String visibility;

    /* {"type":"string","source":"inject"} */
    public String getVisibility() {
      return visibility;
      }
    }
