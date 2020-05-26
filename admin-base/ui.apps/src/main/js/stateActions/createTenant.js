/*-
 * #%L
 * admin base - UI Apps
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
import { LoggerFactory } from '../logger'
import {SUFFIX_PARAM_SEPARATOR} from "../constants";
import setTenant from "./setTenant";

let log = LoggerFactory.logger('createTenant').setLevelDebug()

export default function(me, target) {

    log.fine(target)
    var api = me.getApi()
    return api.createTenant(target.fromName, target.toName, target.title, target.tenantUserPwd, target.colorPalette).then( () => {
        return api.populateTenants().then( () => {
            return setTenant(me, { name : target.toName }).then( () => {
                me.loadContent('/content/admin/pages/welcome.html');
                // path' + SUFFIX_PARAM_SEPARATOR + '/content/'+target.toName)
            })
        })        
    })

}
