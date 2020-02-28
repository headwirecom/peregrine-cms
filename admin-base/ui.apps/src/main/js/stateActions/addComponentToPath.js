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
let log = LoggerFactory.logger('addComponentToPath').setLevelFine()

import { parentPath } from '../utils'

export default function(me, target) {

    log.fine('addComponentToPath()',target)

    let view = me.getView()

    const variationSeparator = target.component ? target.component.indexOf(':') : -1;
    const componentPath = variationSeparator === -1 ? target.component : target.component.slice(0, variationSeparator)
    let variation = variationSeparator === -1 ? undefined : target.component.slice(variationSeparator + 1, target.component.length)

    // resolve path to component name
    let componentName = componentPath ? componentPath.split('/').slice(2).join('-') : target.data.component
    log.fine('load',componentName, 'into edit view (make sure it is available)')
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

    let processed = false;
    // insert new component
    if(targetNode && target.component) {
        processed = true;
        return me.getApi().insertNodeAt(target.pagePath+targetNode.path, componentPath, target.drop, variation)
            .then( (data) => {
                        if(targetNodeUpdate.fromTemplate === true) {
                            return me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                        } else {
                            if(target.drop.startsWith('into')) {
                                Vue.set(targetNodeUpdate, 'children', data.children)
                            }
                            else if(target.drop === 'before' || target.drop === 'after')
                            {
                                Vue.set(targetNodeUpdate, 'children', data.children)
                            }
                            log.fine(data)
                            return
                        }
                    })
    }

    // copy/paste? 
    if(targetNode && target.data) {
        processed = true;
        return me.getApi().insertNodeWithDataAt(target.pagePath+targetNode.path, target.data, target.drop)
                    .then( (data) => {
                        if(targetNodeUpdate.fromTemplate === true) {
                            return me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                        } else {
                            if (target.drop.startsWith('into')) {
                                Vue.set(targetNodeUpdate, 'children', data.children)
                            }
                            else if (target.drop === 'before' || target.drop === 'after') {
                                Vue.set(targetNodeUpdate, 'children', data.children)
                            }
                            log.fine(data)
                            return
                        }
                    })

    }

    if(!processed) {
        // target path does not exist yet
        return me.getApi().insertNodeAt(target.pagePath+target.path, target.component, target.drop)
            .then( (data) => {
                if(!targetNodeUpdate) {
                    return me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
                } else {
                    if(targetNodeUpdate.fromTemplate === true) {
                        return me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
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
    // return new Promise( (resolve, reject) => {
    
    //     if(targetNode) {
    //         if(target.component) {
    //             me.getApi().insertNodeAt(target.pagePath+targetNode.path, componentPath, target.drop, variation)
    //                 .then( (data) => {
    //                     if(targetNodeUpdate.fromTemplate === true) {
    //                         me.getApi().populatePageView(me.getNodeFromView('/pageView/path'))
    //                     } else {
    //                         if(target.drop.startsWith('into')) {
    //                             Vue.set(targetNodeUpdate, 'children', data.children)
    //                         }
    //                         else if(target.drop === 'before' || target.drop === 'after')
    //                         {
    //                             Vue.set(targetNodeUpdate, 'children', data.children)
    //                         }
    //                         log.fine(data)
    //                         resolve()
    //                     }
    //                 })
    //         } else if(target.data) {
    //             me.getApi().insertNodeWithDataAt(target.pagePath+targetNode.path, target.data, target.drop)
    //                 .then( (data) => {
    //                     if(targetNodeUpdate.fromTemplate === true) {
    //                         me.getApi().populatePageView(me.getNodeFromView('/pageView/path')).then(() => { resolve() })
    //                     } else {
    //                         if (target.drop.startsWith('into')) {
    //                             Vue.set(targetNodeUpdate, 'children', data.children)
    //                         }
    //                         else if (target.drop === 'before' || target.drop === 'after') {
    //                             Vue.set(targetNodeUpdate, 'children', data.children)
    //                         }
    //                         resolve()
    //                         log.fine(data)
    //                     }
    //                 })
    //         }
    //     } else {
    //         // target path does not exist yet
    //         me.getApi().insertNodeAt(target.pagePath+target.path, target.component, target.drop)
    //             .then( (data) => {
    //                 if(!targetNodeUpdate) {
    //                     me.getApi().populatePageView(me.getNodeFromView('/pageView/path')).then( () => { resolve() })
    //                 } else {
    //                     if(targetNodeUpdate.fromTemplate === true) {
    //                         me.getApi().populatePageView(me.getNodeFromView('/pageView/path')).then( () => { resolve() })
    //                     } else {
    //                         if(target.drop.startsWith('into')) {
    //                             Vue.set(targetNodeUpdate, 'children', data.children)
    //                         }
    //                         else if(target.drop === 'before' || target.drop === 'after')
    //                         {
    //                             Vue.set(targetNodeUpdate, 'children', data.children)
    //                         }
    //                         log.fine(data)
    //                         resolve()
    //                     }
    //                 }
    //             })
    //     }
    
    // })


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
