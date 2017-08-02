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
import { LoggerFactory } from './logger'
let logger = LoggerFactory.logger('perAdminApp').setLevelDebug()

import PeregrineApi from './api'
import PerAdminImpl from './apiImpl'
import {makePathInfo, pagePathToDataPath, set, get} from './utils'

import StateActions from './stateActions'

window.onpopstate = function(e) {
    if(e && e.state && e.state.path) {
        loadContentImpl(e.state.path, false, true)
    }
}

let view = null
let api = null
let app = null
let loadedComponents = []
let OSBrowser = null

/** dynamic component initializer
 *
 * @private
 */
function loadComponentImpl(name) {
    if(!loadedComponents[name]) {
        logger.fine('loading vuejs component', name)
        var segments = name.split('-')
        for(var i = 0; i < segments.length; i++) {
            segments[i] = segments[i].charAt(0).toUpperCase() + segments[i].slice(1)
        }
        if(window['cmp'+segments.join('')]) {
            Vue.component(name, window['cmp'+segments.join('')])
        }
        loadedComponents[name] = true
    } else {
        logger.fine('component',name, 'already present')
    }
}

function getComponentByNameImpl(name) {
    var segments = name.split('-')
    for(var i = 0; i < segments.length; i++) {
        segments[i] = segments[i].charAt(0).toUpperCase() + segments[i].slice(1)
    }
    return window['cmp'+segments.join('')]
}

function loadData(source) {
    logger.fine('requesting to load data for', source)
    return api.populateByName(source)
}

/** tree walker
 *
 * @private

 * @param node
 * @return {Promise}
 */
function walkTreeAndLoad(node) {

    return new Promise( (resolve, reject) => {
        if(node.component) loadComponentImpl(node.component)
        if(node.dataFrom) {
            get(view, node.dataFrom, node.dataDefault)
        }
        var promises = []
        if(node.source) {
            promises.push(loadData(node.source))
        }
        if(node.children) {
            node.children.forEach(function (child) {
                promises.push(walkTreeAndLoad(child))
            })
        }
        Promise.all(promises).then( () => resolve() )
    })
}

function initPeregrineApp() {
    logger.fine('initPeregrineApp')
    logger.fine(JSON.stringify(view, true, 2))
    app = new Vue({
        el: '#peregrine-adminapp',
        data: view
    });
}

function suffixParamsToModel(suffixParams, mappers) {

    if(mappers) {
        for(var i = 0; i < mappers.length; i+=2) {

            var paramName = mappers[i]
            var paramPath = mappers[i+1]
            var paramValue = suffixParams[paramName]

            if(paramValue) {
                logger.fine('set',paramPath,'to',paramValue)
                set(view, paramPath, paramValue)
            }
        }
    }

}

function processLoaders(loaders) {

    return new Promise( (resolve, reject) => {
        var promises = []
        if(loaders) {
            for(var i = 0; i < loaders.length; i++) {
                var loader = loaders[i].split(':')
                if(loader.length < 2) {
                    logger.fine('unknown loader', loaders[i])
                } else {
                    logger.fine('loading data with', loader[0], loader[1])
                    var pathFrom = loader[1]
                    var dataToLoad = getNodeFromImpl(view, pathFrom)
                    logger.fine(dataToLoad)
                    if(api[loader[0]]) {
                        promises.push(api[loader[0]](dataToLoad))
                    } else {
                        logger.error('missing', loader[0])
                        reject('missing ' + loader[0]+' '+dataToLoad)
                    }
                }
            }
        }
        Promise.all(promises).then( () => resolve() )
    })
}

/** simple data loader
 *
 * @private
 *
 */
function loadContentImpl(initialPath, firstTime, fromPopState) {
    logger.fine('loading content for', initialPath)

    var pathInfo = makePathInfo(initialPath.toString())
    var path = pathInfo.path

    var dataUrl = pagePathToDataPath(path)

    view.status = undefined;

    api.populateUser()
        .then(function() {
            api.populateContent(dataUrl)
                .then( function (data) {
                    logger.fine('got data for', path)
                    walkTreeAndLoad(view.adminPageStaged).then( function() {
                        suffixParamsToModel(pathInfo.suffixParams, view.adminPageStaged.suffixToParameter)

                        processLoaders(view.adminPageStaged.loaders).then( () => {
                            if(firstTime) {
                                view.adminPage = view.adminPageStaged
                                view.status = 'loaded';
                                initPeregrineApp();
                            } else {
                                view.adminPage = view.adminPageStaged
                                view.status = 'loaded';
                            }

                            delete view.adminPageStaged

                            if(!fromPopState) {
                                let params = view.adminPage.suffixToParameter
                                let suffix = ""
                                if(params) {
                                    for(let i = 0; i < params.length; i+=2) {
                                        if(i === 0) {
                                            suffix += '/'
                                        } else {
                                            suffix += '//'
                                        }

                                        suffix += params[0]
                                        suffix += '//'
                                        suffix += getNodeFromImpl(view, params[i+1])
                                    }
                                }
                                let targetPath = initialPath.slice(0, initialPath.indexOf('.html')) + '.html' + suffix

                                if(document.location !== targetPath) {
                                    document.title = getNodeFromImpl(view, '/adminPage/title')
                                    history.pushState({peregrinevue:true, path: targetPath}, targetPath, targetPath)
                                }
                            }
                        })

                    })
                }).catch(function(error) {
                    logger.error("error getting %s %j", dataUrl, error);
                })
        })
}

function findActionInTree(component, command, target) {
    if(component.$options.methods && component.$options.methods[command]) {
        component.$options.methods[command](component, target)
        return true
    } else {
        let children = component.$children
        for(let i = 0; i < children.length; i++) {
            let ret = findActionInTree(children[i], command, target)
            if(ret) return true;
        }
    }
}

function actionImpl(component, command, target) {
    if(component.$options.methods && component.$options.methods[command]) {
        component.$options.methods[command](component, target)
    } else {
        if(component.$parent === component.$root) {
            if(!findActionInTree(component.$root, command, target)) {
                logger.error('action', command, 'not found, ignored, traget was', target)
            }
        } else {
            actionImpl(component.$parent, command, target)
        }
    }
}

function stateActionImpl(name, target) {

    StateActions(name)($perAdminApp, target)

}

function getNodeFromImpl(node, path) {
    return get(node, path)
}

function getNodeFromOrNullImpl(node, path) {

    path = path.slice(1).split('/').reverse()
    while(path.length > 0) {
        var segment = path.pop()
        if(!node[segment]) {
            return null
        }
        node = node[segment]
    }
    return node
}

function getNodeFromWithDefaultImpl(node, path, value) {
    return get(node, path, value)
}

function findNodeFromPathImpl(node, path) {

//    logger.debug(path)
//    logger.debug(JSON.stringify(readNode, true, 2))

    if(node.path === path) return node
    if(node.children) {
        for(var i = 0; i < node.children.length; i++) {
            if(node.children[i].path === path) {
                // found match
                return node.children[i]
            } else if(path.indexOf(node.children[i].path) === 0) {
                var res = findNodeFromPathImpl(node.children[i], path)
                if(res) return res
            }
        }
    }
}

function notifyUserImpl(title, message, cb) {
    set(view, '/state/notification/title', title)
    set(view, '/state/notification/message', message)
    set(view, '/state/notification/onOk', function(){
        set(view, '/state/notification/isVisible', false)
        if(cb && cb !== null) cb()
    })
    set(view, '/state/notification/isVisible', true)
}

function pathBrowserImpl(root, cb) {
    api.populateNodesForBrowser(root, 'pathBrowser').then( () => {
        set(view, '/state/pathbrowser/root', root)
        set(view, '/state/pathbrowser/isVisible', false)
        set(view, '/state/pathbrowser/methods', {
            onShow: function(){ set(view, '/state/pathbrowser/isVisible', true) },
            onHide: function(){ set(view, '/state/pathbrowser/isVisible', false) },
            setItemPath: cb
        })
        view.state.pathbrowser.methods.onShow()
    })
}

function assetBrowserImpl(root, cb) {
    api.populateNodesForBrowser(root, 'pathBrowser').then( () => {
        set(view, '/state/assetbrowser/root', root)
        set(view, '/state/assetbrowser/isVisible', false)
        set(view, '/state/assetbrowser/methods', {
            onShow: function(){ set(view, '/state/assetbrowser/isVisible', true) },
            onHide: function(){ set(view, '/state/assetbrowser/isVisible', false) },
            setItemPath: cb
        })
        view.state.assetbrowser.methods.onShow()
    })
}

function pageBrowserImpl(root, withLinkTab, cb) {
    api.populateNodesForBrowser(root, 'pathBrowser').then( () => {
        set(view, '/state/pagebrowser/root', root)
        set(view, '/state/pagebrowser/isVisible', false)
        set(view, '/state/pagebrowser/withLinkTab', withLinkTab)
        set(view, '/state/pagebrowser/methods', {
            onShow: function(){ set(view, '/state/pagebrowser/isVisible', true) },
            onHide: function(){ set(view, '/state/pagebrowser/isVisible', false) },
            setItemPath: cb
        })
        view.state.pagebrowser.methods.onShow()
    })
}

function isPreviewModeImpl() {
    let mode = getNodeFromOrNullImpl(view, '/state/tools/workspace/view')
    if(mode == null) return false
    return mode === 'preview'
}

function getOSBrowserImpl() {
    if(OSBrowser == null){
        switch(true) {
            case (window.navigator.userAgent.indexOf('Edge') != -1):
                OSBrowser = 'edge'
                break
            case (window.navigator.userAgent.indexOf('Mac') != -1):
                OSBrowser = 'mac'
                break
            case (window.navigator.userAgent.indexOf('Win') != -1):
                OSBrowser = 'win'
                break
            default:
                OSBrowser = 'unknown'
        }
    }
    return OSBrowser
}

const extensions = []

function registerExtensionImpl(id, name) {
    let extensionList = extensions[id]
    if(!extensionList) {
        extensions[id] = [name]
    } else {
        extensionList.push(name)
    }
}

function getExtensionImpl(id) {
    return extensions[id]
}

/**
 * @exports PerAdminApp
 * @namespace PerAdminApp
 *
 */
var PerAdminApp = {

    /**
     *
     * initialize the peregrine administation console with a view object
     *
     * @memberOf PerAdminApp
     * @method
     * @param {Object} perAdminAppView - the basic view object with all the root level nodes defined
     */
    init(perAdminAppView) {
        view = perAdminAppView
        api = new PeregrineApi(new PerAdminImpl(PerAdminApp))
    },

    /**
     * returns a list of all loggers. This is mostly used by the debug console to display/manage the loggers
     *
     * @memberOf PerAdminApp
     * @method
     * @return {*}
     */
    getLoggers() {
        return LoggerFactory.getLoggers()
    },

    /**
     *
     * get a named logger
     *
     * @memberOf PerAdminApp
     * @method
     * @param {string} name - the name of the logger to fetch, always returns a logger
     * @return {Logger}
     */
    getLogger(name) {
        if(!name) return logger
        logger.fine('getting logger for',name)
        return LoggerFactory.logger(name)
    },

    /**
     * convenience method to get the api
     *
     * @memberOf PerAdminApp
     * @method
     * @return {*}
     */
    getApi() {
        return api
    },

    /**
     * convenience method to get the view the admin console is based on
     *
     * @memberOf PerAdminApp
     * @method
     * @return {*}
     */
    getView() {
        return view
    },

    /**
     * load content (eg go to another page)
     *
     * @memberOf PerAdminApp
     * @method
     * @param {string} path - the path to load the content from
     * @param {boolean} firstTime - is vuejs already instantiated?
     */
    loadContent(path, firstTime = false) {
        loadContentImpl(path, firstTime)
    },

    /**
     * loads a component by name. pcms only defines components with a camel cased name
     * cmp{component-name}. In order to keep the amount of components within the vue
     * scope as low as possible this method needs to be called with the component name
     * for vuejs to actually know about it.
     *
     * In the future this method may be used to lazy load the js file for a component
     *
     * @memberOf PerAdminApp
     * @method
     * @param name
     */
    loadComponent(name) {
        loadComponentImpl(name)
    },

    /**
     * Finds a component by the given name.
     *
     * This is helpful if we need to find a component based on a name and execute a method
     * on the component (currently used by the editor before the editing dialog is presented
     * to alter the schema and before a save to trim/rewrite what we save into the backend)
     *
     * @memberOf PerAdminApp
     * @method
     * @param name
     */
    getComponentByName(name) {
        return getComponentByNameImpl(name)
    },

    /**
     * Used to handle admin user interface actions (also see admin-component-action).
     *
     * This method will search for a vue component of the current page starting at `component` and all its
     * parents for a component that contains a method with the name `command`. If no method can be found
     * in the parents then it will search the whole vue tree.
     *
     * Once a method is found it is called with command(me, target)
     *
     * `me` is the actual vue component, `target` is the target object provided to this method
     *
     * @memberOf PerAdminApp
     * @method
     * @param {Vue} component - the vue component calling this action
     * @param {string} command - the method(command) to find in the vue structure
     * @param {Object} target - data to handle the action
     */
    action(component, command, target) {
        actionImpl(component, command, target)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param name
     * @param target
     */
    stateAction(name, target) {
        stateActionImpl(name, target)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param node
     * @param path
     */
    getNodeFrom(node, path) {
        return getNodeFromImpl(node, path)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param path
     */
    getNodeFromView(path) {
        return getNodeFromImpl(this.getView(), path)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param path
     */
    getNodeFromViewOrNull(path) {
        return getNodeFromOrNullImpl(this.getView(), path)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param path
     * @param value
     */
    getNodeFromViewWithDefault(path, value) {
        return getNodeFromWithDefaultImpl(this.getView(), path, value)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param node
     * @param path
     */
    findNodeFromPath(node, path) {
        return findNodeFromPathImpl(node, path)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @return {*}
     */
    getApp() {
        return app;
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param title
     * @param message
     * @param cb
     */
    notifyUser(title, message, cb) {
        notifyUserImpl(title, message, cb)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param rootPath
     * @param cb
     */
    pathBrowser(rootPath, cb) {
        pathBrowserImpl(rootPath, cb)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param rootPath
     * @param cb
     */
    assetBrowser(rootPath, cb) {
        assetBrowserImpl(rootPath, cb)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param rootPath
     * @param cb
     */
    pageBrowser(rootPath, withLinkTab, cb) {
        pageBrowserImpl(rootPath, withLinkTab, cb)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     */
    isPreviewMode() {
        return isPreviewModeImpl()
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     */
    getOSBrowser(){
        return getOSBrowserImpl()
    },


    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param id
     * @param name
     */
    registerExtension(id, name) {
        registerExtensionImpl(id, name)
    },

    /**
     *
     * @memberOf PerAdminApp
     * @method
     * @param id
     */
    getExtension(id) {
        return getExtensionImpl(id)
    }
}

export default PerAdminApp
