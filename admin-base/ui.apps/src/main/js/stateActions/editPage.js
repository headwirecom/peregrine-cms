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
import {EditorTypes, SUFFIX_PARAM_SEPARATOR} from '../constants'
import {set} from '../utils'

let log = LoggerFactory.logger('editPage').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    const view = me.getView()
    const tenant = view.state.tenant

    if(target.startsWith(`/content/${tenant.name}/pages`)) {
        set(view, '/state/tools/page', target)
    } else if(target.startsWith(`/content/${tenant.name}/templates`)) {
        set(view, '/state/tools/template', target)
    }

    set(view, '/state/contentview/editor/type', EditorTypes.PAGE)

    return new Promise( (resolve, reject) => {
        return me.stateAction('showPageInfo', { selected: target}).then( () => {
            me.loadContent('/content/admin/pages/pages/edit.html/path'+SUFFIX_PARAM_SEPARATOR+target)
            resolve()
        })
    })

}
