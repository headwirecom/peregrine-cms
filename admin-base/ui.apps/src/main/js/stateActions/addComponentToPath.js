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
let log = LoggerFactory.logger('addComponentToPath').setLevelDebug()

import { parentPath } from '../utils'

export default function(me, target) {

    log.fine('addComponentToPath()',target)

    let view = me.getView()

    // resolve path to component name
    let componentName = target.component ? target.component.split('/').slice(2).join('-') : target.data.component
    log.fine('load',componentName, 'into edit view (make sure it is available')
    document.getElementById('editview').contentWindow.$peregrineApp.loadComponent(componentName)

    let targetNode = null
    let targetNodeUpdate = null
    if(target.drop.startsWith('into')) {
        targetNode = me.findNodeFromPath(view.pageView.page, target.path)
        targetNodeUpdate = targetNode
    } else if(target.drop === 'before' || target.drop === 'after') {
        var path = parentPath(target.path)
        targetNodeUpdate = me.findNodeFromPath(view.pageView.page, path.parentPath)
        targetNode = me.findNodeFromPath(view.pageView.page, target.path)
    } else {
        log.error('addComponentToPath() target.drop not in allowed values - value was', target.drop)
    }

    if(targetNode) {
        if(target.component) {
            me.getApi().insertNodeAt(target.pagePath+targetNode.path, target.component, target.drop)
                .then( (data) => {
                    if(targetNodeUpdate.fromTemplate === true) {
                        me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                    } else {
                        if(target.drop.startsWith('into')) {
                            Vue.set(targetNodeUpdate, 'children', data.children)
                        }
                        else if(target.drop === 'before' || target.drop === 'after')
                        {
                            Vue.set(targetNodeUpdate, 'children', data.children)
                        }
                        log.fine(data)
                    }
                })
        } else if(target.data) {
            me.getApi().insertNodeWithDataAt(target.pagePath+targetNode.path, target.data, target.drop)
                .then( (data) => {
                    if(targetNodeUpdate.fromTemplate === true) {
                        me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                    } else {
                        if (target.drop.startsWith('into')) {
                            Vue.set(targetNodeUpdate, 'children', data.children)
                        }
                        else if (target.drop === 'before' || target.drop === 'after') {
                            Vue.set(targetNodeUpdate, 'children', data.children)
                        }
                        log.fine(data)
                    }
                })
        }
    } else {
        // target path does not exist yet
        me.getApi().insertNodeAt(target.pagePath+target.path, target.component, target.drop)
            .then( (data) => {
                if(!targetNodeUpdate) {
                    me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                } else {
                    if(targetNodeUpdate.fromTemplate === true) {
                        me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                    } else {
                        if(target.drop.startsWith('into')) {
                            Vue.set(targetNodeUpdate, 'children', data.children)
                        }
                        else if(target.drop === 'before' || target.drop === 'after')
                        {
                            Vue.set(targetNodeUpdate, 'children', data.children)
                        }
                        log.fine(data)
                    }
                }
            })
    }

    // me.getApi().savePageEdit(view.pageView.path, nodeToSave).then( () => {
    //     delete view.state.editor;
    //     set(view, '/state/editorVisible', false)
    // })
    // me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then( (name) => {
    //         log.fine('component name is', name)
    //         set(view, '/state/editor/component', name)
    //         set(view, '/state/editor/path', target)
    //         set(view, '/state/editorVisible', true)
    //         set(view, '/state/rightPanelVisible', true)
    //     }
    // )
}
