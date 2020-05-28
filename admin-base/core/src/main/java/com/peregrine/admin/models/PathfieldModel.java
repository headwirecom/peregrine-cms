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

import static com.peregrine.admin.util.AdminConstants.PATH_FIELD_COMPONENT_PATH;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;
import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
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
@Model(adaptables = Resource.class,
       resourceType = PATH_FIELD_COMPONENT_PATH,
       defaultInjectionStrategy = OPTIONAL,
       adapters = IComponent.class)
@Exporter(name = JACKSON,
          extensions = JSON)

//GEN]
public class PathfieldModel
    extends AbstractComponent {

    public PathfieldModel(Resource r) {
        super(r);
    }

    //GEN[:INJECT
    /* {"type":"string","source":"inject"} */
    @Inject private String dataFrom;

    /* {"type":"string","source":"inject"} */
    @Inject private String dataDefault;


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
