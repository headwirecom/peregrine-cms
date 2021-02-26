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
import {deepClone, get, set} from '../utils'

let log = LoggerFactory.logger('editObject').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    let checksum = ''
    set(me.getView(), '/state/tools/save/confirmed', false)

    me.beforeStateAction((name) => {
        const confirmed = get(me.getView(), '/state/tools/save/confirmed',
            false)
        const currentObject = deepClone(
            me.getNodeFromView('/state/tools/object'))

        if (name !== 'saveObjectEdit') {
            // if there was no change skip asking to save
            const newChecksum = JSON.stringify(
                me.getNodeFromView('/state/tools/object/data'))
            if (confirmed || checksum === newChecksum) {
                return true
            } else {
                return new Promise((resolve) => {
                    $perAdminApp.askUser(
                        'Save Object Edit?',
                        'Would you like to save your object edits?',
                        {
                            yesText: 'Save',
                            noText: 'Cancel',
                            yes() {
                                me.stateAction('saveObjectEdit', {
                                    data: currentObject.data,
                                    path: currentObject.show
                                })
                                resolve()
                            },
                            no() {
                                resolve()
                            }
                        }
                    )
                })
            }
        }
        return true
    })

    let view = me.getView()
    set(me.getView(), `/state/tools/edit`, false)
    me.getApi().populateObject(target.selected, '/state/tools/object', 'data').then( () => {
        checksum = JSON.stringify(me.getNodeFromView('/state/tools/object/data'))
        set(view, '/state/tools/object/show', target.selected)
        set(me.getView(), `/state/tools/edit`, true)
    })
}
