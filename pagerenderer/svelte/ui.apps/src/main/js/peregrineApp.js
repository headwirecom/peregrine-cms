/*-
 * #%L
 * peregrine sveltejs page renderer - UI Apps
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
import { LoggerFactory } from './logger.js'
import experiences from './experiences.js'
import helper from './helper.js'

let log = LoggerFactory.logger('peregrineApp').setDebugLevel()
import state from './state.js'
import merge from './merge.js'
import { pagePathToDataPath, componentNameToVarName } from './util.js'
import {Logger} from "./logger";

function addComponent(name, windowVar) {
    console.log('adding component', name)
//    Svelte.component(name, windowVar)
}

let view
let loadedComponents = []

let perSvelteApp = null

/**
 * disects a path into its parts
 * 
 * @returns Object { path, suffix, suffixParams, hash }
 */
function makePathInfo(path) {

    let hash = ''
    if(path.indexOf('#') >= 0) {
        hash = path.substring(path.indexOf('#'))
        path = path.substring(0, path.indexOf('#'))
    }
    log.fine('makePathInfo for path', path)
    var htmlPos = path.indexOf('.html')
    var pathPart = path
    var suffixPath = ''
    if(htmlPos >= 0) {
        suffixPath = path.slice(htmlPos)
        pathPart = path.slice(0, htmlPos+5)
    }

    var suffixParams = {}
    if(suffixPath.length > 0) {
        suffixPath = suffixPath.slice(6)
        var suffixParamList = suffixPath.split('//')
        for(var i = 0; i < suffixParamList.length; i+= 2) {
            suffixParams[suffixParamList[i]] = suffixParamList[i+1]
        }
    }

    var ret = { path: pathPart, suffix: suffixPath , suffixParams: suffixParams, hash: hash }
    log.fine('makePathInfo res:',ret)
    return ret
}

/**
 * from a given node get the value at path or value if not defined. Path is deliminated by `/`
 * 
 * @param {Object} node 
 * @param {String} path 
 * @param {Any} value 
 */
function get(node, path, value) {

    var svelte = perSvelteApp
    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            if(svelte) {
                // svelte.set(node, segment, {})
            } else {
                node[segment] = {}
            }
        }
        node = node[segment]
    }
    if(value && !node[path[0]]) {
        if(svelte) {
            // Svelte.set(node, path[0], value)
        } else {
            node[path[0]] = value
        }
    }
    if(svelte) {
        svelte.refresh();
    }
    return node[path[0]]
}

/**
 * from a given node set the value at the given path. Path is deliminated by `/`
 * 
 * @param {Object} node 
 * @param {String} path 
 * @param {Any} value 
 */
function set(node, path, value) {

    var svelte = perSvelteApp
    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            if(svelte) {
                // Svelte.set(node, segment, {})
            } else {
                node[segment] = {}
            }
        }
        node = node[segment]
    }
    if(svelte) {
//        Svelte.set(node, path[0], value)
    }
    else {
        node[path[0]] = value
    }
    if(svelte) {
        svelte.refresh();
    }
}

/** init the peregrine app 
 * 
 */
function initPeregrineApp() {

    // todo: how to get this done with svelte
    // Svelte.use(experiences)
    // Svelte.use(helper)

    perSvelteApp = new cmpPagerenderSvelteStructurePage({
        target: document.getElementById('peregrine-app'),
        props: { model: getPerView().page }
    });
}

function registerViewImpl(v) {
    view = v
}

function getView() {
    if(window && window.parent && window.parent.$perAdminView && window.parent.$perAdminView.pageView) {
        log.fine("getVIEW() - window.parent.perAdminView.pageView");
        return window.parent.$perAdminView.pageView
    }
    return view
}

function getPerView() {
    return getView()
}

function loadComponentImpl(name) {
    if(!loadedComponents[name]) {
        log.fine('loading component', name)

        var varName = componentNameToVarName(name)
        if(window[varName]) {
            addComponent(name, window[varName])
        }
        // if we are in edit mode push the component to the perAdminApp as well
        if(window.parent.$perAdminApp && !window.parent[varName]) {
            window.parent[varName] = window[varName]
        }
        loadedComponents[name] = true

    } else {
        log.fine('component %s already loaded', name)
    }


}


function walkTreeAndLoad(node) {

    if(node.component) loadComponentImpl(node.component)
    if(node.children) {
        node.children.forEach(function (child) {
            walkTreeAndLoad(child)
        })
    }
}

function getNodeFromImpl(node, path) {
    return get(node, path)
}

function processLoaders(loaders) {

    return new Promise( (resolve, reject) => {
        var promises = []
        if(loaders) {
            for(var i = 0; i < loaders.length; i++) {
                var loader = loaders[i].split(':')
                if(loader.length < 2) {
                    log.fine('unknown loader', loaders[i])
                } else {
                    log.fine('loading data with', loader[0], loader[1])
                    var pathFrom = loader[1]
                    var dataToLoad = getNodeFromImpl(view, pathFrom)
                    log.fine(dataToLoad)
                    if(api[loader[0]]) {
                        promises.push(api[loader[0]](dataToLoad))
                    } else {
                        log.error('missing', loader[0])
                        reject('missing ' + loader[0]+' '+dataToLoad)
                    }
                }
            }
        }
        Promise.all(promises).then( () => resolve() )
    })
}

function processLoadedContent(data, path, firstTime, fromPopState) {
    walkTreeAndLoad(data)

    if(data.description) document.getElementsByTagName('meta').description.content=data.description
    if(data.tags) document.getElementsByTagName('meta').keywords.content=data.tags

    if(data.suffixToParameter) {
        const pathInfo = makePathInfo(path)
        for(let i = 0; i < data.suffixToParameter.length; i+=2) {
            const name = data.suffixToParameter[i]
            const location =  data.suffixToParameter[i+1]
            set(getPerView(), location, pathInfo.suffixParams[name])
        }
    }
    processLoaders(data.loaders).then( () => {

        log.fine('first time', firstTime)

        getPerView().page = data;
        getPerView().path = path.slice(0, path.indexOf('.html'));
        getPerView().status = 'loaded';
        if(firstTime) {
            initPeregrineApp();
        }

        if(perSvelteApp) {
            perSvelteApp.refresh(getPerView().page);
        }

        if(document.location !== path && !fromPopState) {
            log.fine("PUSHSTATE : " + path);
            document.title = getPerView().page.title
            var url = document.location.href
            var domains = (getPerView().page.domains)
            var newLocation = path
            if (domains) {
                for (var i = 0; i < domains.length; i++) {
                    var domain = domains[i]
                    if (url.startsWith(domain)) {
                        newLocation = '/' + path.split('/').slice(4).join('/')
                    }
                }
            }
            if(firstTime) {
                history.replaceState({peregrinesvelte: true, path: path}, path, newLocation)
            } else {
                history.pushState({peregrinesvelte: true, path: path}, path, newLocation)
            }
            scroll(0, 0)

            // Create the event.
            var event = document.createEvent('Event')

            // Define that the event name is 'build'.
            event.initEvent('pageRendered', true, true)

            // target can be any Element or other EventTarget.
            window.dispatchEvent(event)
        }

    })
}

function loadContentImpl(path, firstTime, fromPopState) {

    log.fine('loading content for', path, firstTime, fromPopState)

    var dataUrl = pagePathToDataPath(path);
    log.fine(dataUrl)
    getPerView().status = undefined;
    axios.get(dataUrl).then(function (response) {
        log.fine('got data for', path)
        processLoadedContent(response.data, path, firstTime, fromPopState)
    }).catch(function(error) {
        log.error("error getting %s %j", dataUrl, error);
    });
}

function isAuthorModeImpl() {

    if(window && window.parent && window.parent.$perAdminView && window.parent.$perAdminView.pageView) {
        return true
    }
    return false
    
}

var peregrineApp = {

    registerView: function(view) {
        registerViewImpl(view)
    },

    loadContent: function(path, firstTime = false, fromPopState = false) {
        loadContentImpl(path, firstTime, fromPopState)
    },

    logger: function(name) {
        return LoggerFactory.logger(name)
    },

    loadComponent: function(name) {
        loadComponentImpl(name)
    },

    getPerSvelteApp: function() {
        return perSvelteApp
    }, 
    isAuthorMode: function() {
        return isAuthorModeImpl()        
    },

    getView: function() {
       return getPerView()
    },

    resolveComponent: function(component) {
        return window[componentNameToVarName(component)];
    }


}

/**
 * you should use this object as follows:
 *
 * var $perView = {};
 * $peregrineApp.registerView($perView)
 * $peregrineApp.loadContent('/content/sites/example.html')
 *
 */
export default peregrineApp
