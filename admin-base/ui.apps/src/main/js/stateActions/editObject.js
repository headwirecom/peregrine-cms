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
let log = LoggerFactory.logger('editObject').setLevelDebug()

import { set } from '../utils'

export default function(me, target) {

    log.fine(target)

    let checksum = ''

    me.beforeStateAction( function(name) {
        if(name !== 'saveObjectEdit') {
            // if there was no change skip asking to save
            if(checksum === JSON.stringify(me.getNodeFromView('/state/tools/object/data'))) {
                return true
            }
            const yes = confirm('save edit?')
            if(yes) {
                const currentObject = me.getNodeFromView("/state/tools/object")
                me.stateAction('saveObjectEdit', { data: currentObject.data, path: currentObject.show })
            }
        }
        return true
    })

    let view = me.getView()
    Vue.set(me.getNodeFromView('/state/tools'), 'edit', true)
    me.getApi().populateObject(target.selected, '/state/tools/object', 'data').then( () => {
        checksum = JSON.stringify(me.getNodeFromView('/state/tools/object/data'))
        set(view, '/state/tools/object/show', target.selected)
    })
}
