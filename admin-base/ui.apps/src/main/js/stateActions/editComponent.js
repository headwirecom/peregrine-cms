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
let log = LoggerFactory.logger('editComponent').setLevelDebug()

import { set } from '../utils'

function bringUpEditor(me, view, target) {
    log.debug('Bring Up Editor, ')
    me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then( (name) => {
            log.fine('component name is', name)
            set(view, '/state/editor/component', name)
            set(view, '/state/editor/path', target)
            set(view, '/state/editorVisible', true)
            set(view, '/state/rightPanelVisible', true)
        }
    ).catch( error => {
        log.debug('Failed to show editor: ' + error)
        $perAdminApp.notifyUser('error', 'was not able to bring up editor for the selected component')
    })
}

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    if(view.state.editorVisible) {
        me.getApi().populatePageView(view.pageView.path).then( () => {
            bringUpEditor(me, view, target)
        })
    } else {
        bringUpEditor(me, view, target)
    }


}
