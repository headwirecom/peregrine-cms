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
import {set} from '../utils'

let log = LoggerFactory.logger('editComponent').setLevelDebug()

function bringUpEditor(me, view, target) {
    log.fine('Bring Up Editor, ')

    const componentNode = me.findNodeFromPath(view.pageView.page, target)
    const originalData = componentNode ? JSON.parse(JSON.stringify(componentNode)) : null

    const beforeUnloadHandler = function(e) {
        if (!view.pageView || !view.pageView.page) return;
        if (!view.state.editor || !view.state.editor.originalData) return;
        const currentNode = me.findNodeFromPath(view.pageView.page, view.state.editor.path)
        if (!currentNode) return;
        const replacer = (k, v) => k === '_opDeleteProps' || k === 'children' || v === null || v === '' ? undefined : v
        const originalStr = JSON.stringify(view.state.editor.originalData, replacer)
        const currentStr = JSON.stringify(currentNode, replacer)
        if (originalStr !== currentStr) {
            e.preventDefault();
            e.returnValue = '';
            return '';
        }
    };
    $perAdminApp.setBeforeUnloadHandler(beforeUnloadHandler);

    me.beforeStateAction(function(name) {
        if (name === 'savePageEdit' || name === 'deletePageNode' || name === 'editComponent') {
            return true;
        }
        if (!view.state.editor || !view.state.editor.originalData) {
            return true;
        }
        const currentNode = me.findNodeFromPath(view.pageView.page, view.state.editor.path)
        if (!currentNode) {
            return true;
        }
        const originalStr = JSON.stringify(view.state.editor.originalData, (k, v) => k === '_opDeleteProps' || k === 'children' || v === null || v === '' ? undefined : v)
        const currentStr = JSON.stringify(currentNode, (k, v) => k === '_opDeleteProps' || k === 'children' || v === null || v === '' ? undefined : v)
        if (originalStr === currentStr) {
            return true;
        }
        return new Promise((resolve) => {
            $perAdminApp.askUser('Save Page Edit?', 'Would you like to save your page edits?', {
                defaultFocus: 'keepEditing',
                yesText: 'Save',
                noText: 'Discard Changes',
                keepEditingText: 'Keep Editing',
                yes() {
                    const page = view.pageView.page;
                    const path = view.state.editor.path;
                    const data = me.findNodeFromPath(page, path);
                    me.stateAction('savePageEdit', { pagePath: view.pageView.path, path, data}).then(() => {
                        $perAdminApp.clearBeforeStateActions();
                        $perAdminApp.clearBeforeUnloadHandler();
                        resolve(true);
                    });
                },
                no() {
                    $perAdminApp.clearBeforeStateActions();
                    $perAdminApp.clearBeforeUnloadHandler();
                    resolve(true);
                },
                keepEditing() {
                    resolve(false);
                }
            });
        });
    });

    return new Promise((resolve, reject) => {
        return me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then((name) => {
                log.fine('component name is', name)
                set(view, '/state/editor/component', name)
                set(view, '/state/editor/path', target)
                set(view, '/state/editorVisible', true)
                set(view, '/state/rightPanelVisible', true)
                set(view, '/state/editor/originalData', originalData)
                resolve()
            }
        ).catch(error => {
            log.debug('Failed to show editor: ' + error)
            $perAdminApp.notifyUser('error', 'was not able to bring up editor for the selected component')
            reject()
        })
    })
}

export default function(me, target) {
    log.fine(target)
    let view = me.getView()

    if (view.state.editor && view.state.editor.path === target) {
        return Promise.resolve()
    }

    return new Promise((resolve, reject) => {
        bringUpEditor(me, view, target).then(() => { resolve() }).catch(() => reject())
    })
}
