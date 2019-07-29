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
let log = LoggerFactory.logger('moveComponentToPath').setLevelDebug()

import { parentPath } from '../utils'

export default function(me, target) {

    log.fine('moveComponentToPath()',target)

    let view = me.getView()

    let targetNodeUpdate = null
    if(target.drop.startsWith('into')) {
        targetNodeUpdate = me.findNodeFromPath(view.pageView.page, target.path)
    } else if(target.drop === 'before' || target.drop === 'after') {
        var path = parentPath(target.path)
        targetNodeUpdate = me.findNodeFromPath(view.pageView.page, path.parentPath)
    } else {
        log.error('addComponentToPath() target.drop not in allowed values - value was', target.drop)
    }

    return new Promise( (resolve, reject) => {
        me.getApi().moveNodeTo(target.pagePath+target.path, target.pagePath+target.component, target.drop)
        .then( (data) => {
            var node = me.findNodeFromPath(view.pageView.page, target.component)
            var parent = me.findNodeFromPath(view.pageView.page, parentPath(target.component).parentPath)
            let index = parent.children.indexOf(node)
            parent.children.splice(index, 1)
            if(targetNodeUpdate.fromTemplate === true) {
                me.getApi().populatePageView(me.getNodeFromView('/pageView/path')).then( () => { resolve() })
            } else {
                if(target.drop.startsWith('into')) {
                    Vue.set(targetNodeUpdate, 'children', data.children)
                }
                else if(target.drop === 'before' || target.drop === 'after')
                {
                    Vue.set(targetNodeUpdate, 'children', data.children)
                }
                resolve()
            }
        })
    })
}
