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

import static com.peregrine.admin.util.AdminConstants.COL_COMPONENT_PATH;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;

import com.peregrine.nodetypes.models.Container;
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
    "classes": {
      "type": "string",
      "source": "inject"
    }
  },
  "propertyNames": [
    "component",
    "path",
    "classes"
  ],
  "modelName": "Col",
  "package": "com.peregrine.admin.models",
  "componentPath": "admin/components/col",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(adaptables = Resource.class,
       resourceType = COL_COMPONENT_PATH,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
       adapters = IComponent.class)
@Exporter(name = JACKSON,
          extensions = JSON)
//GEN]
public class ColModel
    extends Container {

    public ColModel(Resource r) {
        super(r);
    }

    //GEN[:INJECT
    /* {"type":"string","source":"inject"} */
    @Inject private String classes;


    //GEN]

    //GEN[:GETTERS
    /* {"type":"string","source":"inject"} */
    public String getClasses() {
        return classes;
    }


    //GEN]

}
