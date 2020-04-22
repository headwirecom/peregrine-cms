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
import {LoggerFactory} from '../logger'
import {set, jsonEqualizer} from '../utils'

let log = LoggerFactory.logger('editComponent').setLevelDebug()

function bringUpEditor(me, view, target) {
    log.fine('Bring Up Editor, ')

    me.beforeStateAction( function(name) {
        return new Promise( (resolve, reject) => {
            const current = JSON.stringify(view.pageView.page, jsonEqualizer, 2)
            if(name !== 'savePageEdit' && name !== 'deletePageNode') {
                if(current === view.state.editor.checksum) {
                    resolve(true)
                } else {
                    $perAdminApp.askUser('Save Page Edit?', 'Would you like to save your page edits?', {
                        yesText: 'Save',
                        noText: 'Cancel',
                        yes() {
                            const page = view.pageView.page;
                            const path = view.state.editor.path;
                            const data = me.findNodeFromPath(page, path);
                            me.stateAction('savePageEdit', { pagePath: view.pageView.path, path, data}).then( () => {
                                resolve(true)
                            })
                        },
                        no() {
                            resolve(false)
                        }
                    })
                }
            } else {
                resolve(true)
            }
        });
    })

    return new Promise( (resolve, reject) => {
        me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then( (name) => {
                log.fine('component name is', name)
                set(view, '/state/editor/component', name)
                set(view, '/state/editor/path', target)
                set(view, '/state/editorVisible', true)
                set(view, '/state/rightPanelVisible', true)
                set(view, '/state/editor/checksum', JSON.stringify(view.pageView.page, jsonEqualizer, 2))
                resolve()
            }
        ).catch( error => {
            log.debug('Failed to show editor: ' + error)
            $perAdminApp.notifyUser('error', 'was not able to bring up editor for the selected component')
            reject()
        })
    })

}

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    return new Promise( (resolve, reject) => {
        if(view.state.editorVisible) {
            me.getApi().populatePageView(view.pageView.path).then( () => {
                bringUpEditor(me, view, target).then( () => { resolve() }).catch( () => reject() )
            })
        } else {
            bringUpEditor(me, view, target).then( () => { resolve() }).catch( () => reject() )
        }
    })

}
