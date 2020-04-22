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

const consoleERROR = console.error

if($perAdminApp) {
    $perAdminApp.getView().admin.consoleErrors = false
}

console.error= function() {
    if($perAdminApp) {
        $perAdminApp.getView().admin.consoleErrors = true
    }
    consoleERROR.apply(this, arguments)
}

import {LoggerFactory} from './logger'
import i18n from './i18n'
import experiences from './experiences'
import PeregrineApi from './api'
import PerAdminImpl from './apiImpl'
import {get, makePathInfo, pagePathToDataPath, set} from './utils'

import StateActions from './stateActions'

import {SUFFIX_PARAM_SEPARATOR} from './constants'

let logger = LoggerFactory.logger('perAdminApp').setLevelDebug()

/**
 * registers a pop state listener for the adminui to track back/forward button and loads
 * the correct screen accordingly
 *
 * @private
 * @param e
 */
window.onpopstate = function(e) {
    if(e && e.state && e.state.path) {
        loadContentImpl(e.state.path, false, true)
    }
}

/**
 * the view object containing all the data used to render the UI

 * @private
 * @type {null}
 */
let view = null

/**
 * reference to the API Interface that's used to implement all backend functionality
 *
 * @private
 * @type {null}
 */
let api = null

/**
 * the vuejs app that is created to render the admin ui
 *
 * @private
 * @type {null}
 */
let app = null

/**
 * list of currently loaded vuejs components
 *
 * @private
 * @type {Array}
 */
let loadedComponents = []

/** ?
 *
 * @private
 * @type {null}
 */
let OSBrowser = null

/**
 * list of all the currently installed extensions
 *
 * @private
 * @type {Array}
 */
const extensions = []

/**
 * dynamic component initializer\ - this function takes a name of a component and tries to
 * find the matching variable in the global scope if the component has not been registered
 * with vuejs yet.
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

/**
 * gets the global varibale backing a component by it's name (vuejs - name converted to camel case
 * and cmp added in front of it)
 *
 * @private
 * @param name
 * @return {*}
 */
function getComponentByNameImpl(name) {
    var segments = name.split('-')
    for(var i = 0; i < segments.length; i++) {
        segments[i] = segments[i].charAt(0).toUpperCase() + segments[i].slice(1)
    }
    return window['cmp'+segments.join('')]
}

/**
 * loads data into the view through the populateByName function of the api
 *
 * @private
 * @param source
 * @return {*}
 */
function loadData(source) {
    logger.fine('requesting to load data for', source)
    return api.populateByName(source)
}

/**
 * walks the current node tree and loads all the necessary data related to the page
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

/**
 * initializes the vuejs app
 *
 * @private
 */
function initPeregrineApp() {
    logger.fine('initPeregrineApp')
    logger.fine(JSON.stringify(view, true, 2))

    Vue.config.productionTip = false
    Vue.use(i18n)
    Vue.use(experiences)
    const lang = view.state.language
    const i18nData = view.admin.i18n

    app = new Vue({
        el: '#peregrine-adminapp',
        data: view
    });

    const state = sessionStorage.getItem('perAdminApp.state')
    const admin = sessionStorage.getItem('perAdminApp.admin')

    if(state && admin) {
        view.state = JSON.parse(state)
        view.admin = JSON.parse(admin)

        // make i18n and language selection survive session storage
        view.state.language = lang
        view.admin.i18n = i18nData
    }

    app.$watch('state', function(newVal, oldVal) {
        sessionStorage.setItem('perAdminApp.state', JSON.stringify(newVal))
    }, { deep: true })
    app.$watch('admin', function(newVal, oldVal) {
        sessionStorage.setItem('perAdminApp.admin', JSON.stringify(newVal))
    }, { deep: true })
}

/**
 * conversion of suffix paramters to model
 *
 * @private
 * @param suffixParams
 * @param mappers
 */
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

/**
 * process all the defined loaders from the data model
 *
 * @private
 * @param loaders
 * @return {Promise}
 */
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

/**
 * Implementation of the loadContent function of the $perAdminApp interface.
 *
 * checks if user is logged in then loads the data for the given page and processes the tree
 * for all loaders and component registration. Finally updates the history and applies the
 * changes to the view
 *
 * @private
 *
 */
function loadContentImpl(initialPath, firstTime, fromPopState) {
    logger.fine('loading content for', initialPath)
    view.admin.consoleErrors = false

    if(!runBeforeStateActions() ) {
        logger.fine('not allowed to switch state')
        return
    }

    var pathInfo = makePathInfo(initialPath.toString())
    var path = pathInfo.path

    var dataUrl = pagePathToDataPath(path)

    view.status = undefined;

    api.populateUser()
        .then(function() {
            api.populateContent(dataUrl)
                .then( function () {
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
                                            suffix += SUFFIX_PARAM_SEPARATOR
                                        }

                                        suffix += params[0]
                                        suffix += SUFFIX_PARAM_SEPARATOR
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

/**
 * finds a UI action in the node tree that makes up the currently rendered page. Walks the full tree
 *
 * @private
 * @param component
 * @param command
 * @param target
 * @return {boolean}
 */
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

/**
 * implementation of $perAdminApp.action()
 *
 * @private
 * @param component
 * @param command
 * @param target
 */
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

/**
 * keeps a list of the current state actions we want to perform after another state action is
 * triggered (for example to ask if a change in the editor should be saved)
 *
 * @type {Array}
 */
let beforeStateActions = []

function beforeStateActionImpl(fun) {
    beforeStateActions.push(fun)
}

function runBeforeStateActions(name) {
    // execute all before state actions
    const actions = []
    if(beforeStateActions.length > 0) {
        for(let i = 0; i < beforeStateActions.length; i++) {
            actions.push(beforeStateActions[i](name))
        }
    }

    return new Promise( (resolve) => {
        Promise.all(actions).then( () => {
            beforeStateActions = []
            resolve()
        })
    });
    // console.log(ret);

    // // clear the actions
    // return true
}

const waitStack = []

function enterWaitState() {
    waitStack.push('wait')
    setTimeout( function() {
        if(waitStack.length > 0) {
            document.getElementById('waitMask').style.display = 'inherit'
        }
    }, 100)
}

function exitWaitState() {
    waitStack.pop()
    if(waitStack.length === 0) {
        document.getElementById('waitMask').style.display = 'none'
    }
}

/**
 * implementation of $perAdminApp.stateAction()
 *
 * @private
 * @param name
 * @param target
 */
function stateActionImpl(name, target) {
    enterWaitState()
    return new Promise( (resolve, reject) => {
        runBeforeStateActions(name).then( () => {
            try {
                const stateAction = StateActions(name)
                const action = stateAction($perAdminApp, target)
                Promise.resolve(action).then(result => {
                    exitWaitState()
                    if(result && result.startsWith('Uncaught (in promise')) {
                        notifyUserImpl('error', result)
                        reject()
                    } else {
                        resolve()
                    }
                }).catch(error => {
                    exitWaitState()
                    notifyUserImpl('error', error)
                    reject()
                })
            } catch(error) {
                exitWaitState()
                reject(error)
            }
        })
    })

}

/**
 * implementation of $perAdminApp.getNodeFrom()
 *
 * @private
 * @param node
 * @param path
 * @return {*}
 */
function getNodeFromImpl(node, path) {
    return get(node, path)
}

/**
 * implementation of $perAdminApp.getNodeFromOrNull()
 *
 * @private
 * @param node
 * @param path
 * @return {*}
 */
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

/**
 * implementation of $perAdminApp.getNodeFromWithDefault()
 *
 * @private
 * @param node
 * @param path
 * @param value
 * @return {*}
 */
function getNodeFromWithDefaultImpl(node, path, value) {
    return get(node, path, value)
}

/**
 * implementation of $perAdminApp.findNodeFromPath()
 *
 * @private
 * @param node
 * @param path
 * @return {*}
 */
function findNodeFromPathImpl(node, path) {
    if(node.path === path) return node
    if(node.children) {
        for(var i = 0; i < node.children.length; i++) {
            if(node.children[i].path === path) {
                // found match
                return node.children[i]
            } else if(path && path.indexOf(node.children[i].path) === 0) {
                var res = findNodeFromPathImpl(node.children[i], path)
                if(res) return res
            }
        }
    }
}

/**
 * implementation of $perAdminApp.notifyUser()
 *
 * @private
 * @param title
 * @param message
 * @param options
 */
function notifyUserImpl(title, message, options) {
    set(view, '/state/notification/title', title)
    set(view, '/state/notification/message', message)
    $('#notifyUserModal').modal('open', options)
}

/**
 * implementation of $perAdminApp.askUser()
 *
 * @private
 * @param title
 * @param message
 * @param options
 */
function askUserImpl(title, message, options) {
    set(view, '/state/notification/title', title)
    set(view, '/state/notification/message', message)
    let yesText = options.yesText ? options.yesText : 'Yes'
    let noText = options.noText ? options.noText : 'No'
    set(view, '/state/notification/yesText', yesText)
    set(view, '/state/notification/noText', noText)
    options.dismissible = false
    options.takeAction = false
    options.complete = function() {
        const answer = $('#askUserModal').modal('getInstance').options.takeAction;
        if(answer && options.yes) {
            options.yes()
        } else if(options.no) {
            options.no()
        }
    }
    $('#askUserModal').modal(options)
    $('#askUserModal').modal('open')
}

/**
 * implementation of $perAdminApp.promptUser()
 *
 * @private
 * @param title
 * @param message
 * @param options
 */
function promptUserImpl(title, message, options) {
    set(view, '/state/notification/title', title)
    set(view, '/state/notification/message', message)
    let yesText = options.yesText ? options.yesText : 'Ok'
    let noText = options.noText ? options.noText : 'Cancel'
    set(view, '/state/notification/yesText', yesText)
    set(view, '/state/notification/noText', noText)

    options.dismissible = false
    options.takeAction = false
    options.complete = function() {
        const answer = $('#promptUserModal').modal('getInstance').options.takeAction;
        const value = $('#promptUserModal').modal('getInstance').options.value;
        if(answer && options.yes) {
            options.yes(value)
        } else if(options.no) {
            options.no()
        }
    }
    $('#promptUserModal').modal(options)
    $('#promptUserModal').modal('open')
}

/**
 * implementation of $perAdminApp.isPreviewMode()
 *
 * @private
 * @return {boolean}
 */
function isPreviewModeImpl() {
    let mode = getNodeFromOrNullImpl(view, '/state/tools/workspace/view')
    if(mode == null) return false
    return mode === 'preview'
}

/**
 * implementation of $perAdminApp.getOSBrowser()
 *
 * @private
 * @return {null}
 */
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

/**
 * implementation of $perAdminApp.registerExtension()
 *
 * @private
 * @param id
 * @param name
 */
function registerExtensionImpl(id, name) {
    let extensionList = extensions[id]
    if(!extensionList) {
        extensions[id] = [name]
    } else {
        extensionList.push(name)
    }
}

/**
 * implementation of $perAdminApp.getExtension()
 *
 * @private
 * @param id
 * @return {*}
 */
function getExtensionImpl(id) {
    return extensions[id]
}

function loadi18nImpl() {
    if(!view.state.language) {
        Vue.set(view.state, 'language', 'en')
    }
    api.populateI18N(view.state.language)
}

/**
 * helper function to call the backend every minute to keep the session alive
 */
function sessionKeepAlive() {
    setInterval(function() {
        api.populateUser();
    },1000 * 60);
}

/**
 * @exports PerAdminApp
 * @namespace PerAdminApp
 *
 */
var PerAdminApp = {

    eventBus: new Vue(),

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
        sessionKeepAlive();
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
     * trigger a registered state action with the provided target information
     *
     * @memberOf PerAdminApp
     * @method
     * @param name
     * @param target
     */
    stateAction(name, target) {
        return stateActionImpl(name, target)
    },

    /**
     * get a node from the given objec with the given path
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
     * get a node from the current view object with the given path
     *
     * @memberOf PerAdminApp
     * @method
     * @param path
     */
    getNodeFromView(path) {
        return getNodeFromImpl(this.getView(), path)
    },

    /**
     * get a node from the current view object with the given path or null if not defined
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
     * get a node from the current view object with the given path and set value as the
     * default if not yet defined
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
     *  find a node with a property path that is equal to path. This also looks at all the
     *  children of the node.
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
     * get the backing application that has been created for peregrine (vue object)
     *
     * @memberOf PerAdminApp
     * @method
     * @return {*}
     */
    getApp() {
        return app;
    },

    /**
     * modal with the given title and message to notify the user, calls the callback on close if provided
     *
     *
     * @memberOf PerAdminApp
     * @method
     * @param title
     * @param message
     * @param options
     */
    notifyUser(title, message, options) {
        notifyUserImpl(title, message, options)
    },

    /**
     * modal with the given title and message to ask the user, calls the callback on close if provided
     *
     *
     * @memberOf PerAdminApp
     * @method
     * @param title
     * @param message
     * @param options
     */
    askUser(title, message, options) {
        askUserImpl(title, message, options)
    },

    /**
     * modal with the given title and message to ask the user and a field for user input,
     * calls the callback on close if provided
     *
     *
     * @memberOf PerAdminApp
     * @method
     * @param title
     * @param message
     * @param options
     */
    promptUser(title, message, options) {
        promptUserImpl(title, message, options)
    },


    /**
     * returns true if the editor is currently in preview mode
     *
     * @memberOf PerAdminApp
     * @method
     */
    isPreviewMode() {
        return isPreviewModeImpl()
    },

    /**
     * returns the OS? Used for some browser dependent code
     *
     * @memberOf PerAdminApp
     * @method
     */
    getOSBrowser(){
        return getOSBrowserImpl()
    },


    /**
     * allows for an extension to be registered to implement custom UI code in the admin console
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
     * entry point for front end to grab extensions to the UI
     *
     * @memberOf PerAdminApp
     * @method
     * @param id
     */
    getExtension(id) {
        return getExtensionImpl(id)
    },

    getExperiences() {
        const experiences = this.getNodeFromView('/state/currentExperiences')
        if(!experiences) {
            Vue.set(this.getView().state, 'currentExperiences', [])
            return this.getNodeFromView('/state/currentExperiences')
        }
        return experiences
    },

    loadi18n() {
        loadi18nImpl()
    },

    forceFullRedraw() {
        this.getView().adminPage = JSON.parse(JSON.stringify(this.getView().adminPage))
    },

    beforeStateAction(fun) {
        beforeStateActionImpl(fun)
    },

    normalizeString(val, separator='-') {
        return val.normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/\W/g, separator)
            .toLowerCase();
    }

}

export default PerAdminApp
