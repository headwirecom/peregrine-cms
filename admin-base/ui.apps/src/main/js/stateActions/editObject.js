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

    $perAdminApp.clearBeforeStateActions();
    $perAdminApp.clearBeforeUnloadHandler();

    let checksum = ''
    let originalData = null
    set(me.getView(), '/state/tools/save/confirmed', false)

    const beforeUnloadHandler = function(e) {
        const objectData = me.getNodeFromView('/state/tools/object/data');
        if (!objectData) {
            $perAdminApp.clearBeforeUnloadHandler();
            return;
        }
        const confirmed = get(me.getView(), '/state/tools/save/confirmed', false)
        const newChecksum = JSON.stringify(objectData)
        if (!confirmed && checksum !== newChecksum) {
            e.preventDefault();
            e.returnValue = '';
            return '';
        }
    };
    $perAdminApp.setBeforeUnloadHandler(beforeUnloadHandler);

    me.beforeStateAction((name) => {
        if (name === 'saveObjectEdit') {
            return true;
        }

        const objectData = me.getNodeFromView('/state/tools/object/data');
        if (!objectData) {
            $perAdminApp.clearBeforeStateActions();
            $perAdminApp.clearBeforeUnloadHandler();
            return true;
        }

        const confirmed = get(me.getView(), '/state/tools/save/confirmed', false);
        const newChecksum = JSON.stringify(objectData);
        
        if (confirmed || checksum === newChecksum) {
            return true;
        }

        return new Promise((resolve) => {
            $perAdminApp.askUser(
                'Save Object Edit?',
                'Would you like to save your object edits?',
                {
                    defaultFocus: 'keepEditing',
                    yesText: 'Save',
                    noText: 'Discard Changes',
                    keepEditingText: 'Keep Editing',
                    yes() {
                        const currentObject = deepClone(me.getNodeFromView('/state/tools/object'));
                        me.stateAction('saveObjectEdit', {
                            data: currentObject.data,
                            path: currentObject.show
                        }).then(() => {
                            $perAdminApp.clearBeforeStateActions();
                            $perAdminApp.clearBeforeUnloadHandler();
                            resolve(true);
                        }).catch(() => {
                            $perAdminApp.clearBeforeStateActions();
                            $perAdminApp.clearBeforeUnloadHandler();
                            resolve(true);
                        });
                    },
                    no() {
                        if (originalData !== null) {
                            set(me.getView(), '/state/tools/object/data', deepClone(originalData));
                        }
                        checksum = JSON.stringify(originalData);
                        $perAdminApp.clearBeforeStateActions();
                        $perAdminApp.clearBeforeUnloadHandler();
                        resolve(true);
                    },
                    keepEditing() {
                        resolve(false);
                    }
                }
            );
        });
    });

    let view = me.getView()
    set(me.getView(), `/state/tools/edit`, false)
    me.getApi().populateObject(target.selected, '/state/tools/object', 'data').then( () => {
        const loadedData = me.getNodeFromView('/state/tools/object/data')
        checksum = JSON.stringify(loadedData)
        originalData = deepClone(loadedData)
        set(view, '/state/tools/object/show', target.selected)
        set(me.getView(), `/state/tools/edit`, true)
    })
}
