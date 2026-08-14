/*-
 * #%L
 * peregrine vuejs page renderer - UI Apps
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


let view
let loadedComponents = []

let perVueApp = null

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

function get(node, path, value) {

    var vue = perVueApp
    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            if(vue) {
                Vue.set(node, segment, {})
            } else {
                node[segment] = {}
            }
        }
        node = node[segment]
    }
    if(value && !node[path[0]]) {
        if(vue) {
            Vue.set(node, path[0], value)
        } else {
            node[path[0]] = value
        }
    }
    return node[path[0]]
}

function set(node, path, value) {

    var vue = perVueApp
    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            if(vue) {
                Vue.set(node, segment, {})
            } else {
                node[segment] = {}
            }
        }
        node = node[segment]
    }
    if(vue) {
        Vue.set(node, path[0], value)
    }
    else {
        node[path[0]] = value
    }
}

function initPeregrineApp() {

    Vue.config.productionTip = false
    Vue.use(experiences)
    Vue.use(helper)

    perVueApp = new Vue({
        el: '#peregrine-app',
        data: getPerView()
    });
}

function registerViewImpl(v) {
    view = v
}

function getView() {
    try {
        if(window && window.parent && window.parent.$perAdminView && window.parent.$perAdminView.pageView) {
            var mode = window.frameElement.attributes['data-per-mode'] ? window.frameElement.attributes['data-per-mode'].value : null;
            if(mode === 'tutorial') {
                return view;
            } else {
                log.fine("getVIEW() - window.parent.perAdminView.pageView");
                return window.parent.$perAdminView.pageView
            }
        }
        return view
    } catch (error) {
        // different origin
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
            Vue.component(name, window[varName])
        }
        // if we are in edit mode push the component to the perAdminApp as well
        try {
            if(window.parent.$perAdminApp && !window.parent[varName]) {
                window.parent[varName] = window[varName]
            }
        } catch (error) {
            // same origin
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
    data = window.$perProcessData !== undefined ? window.$perProcessData(data) : data
    walkTreeAndLoad(data)

    if(data.description) document.getElementsByTagName('meta').description.content=data.description
    if(data.tags) document.getElementsByTagName('meta').keywords.content=data.tags.map( tag => tag.name )

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

        if(document.location !== path && !fromPopState) {
            log.fine("PUSHSTATE : " + path);
            document.title = getPerView().page.title + ' | ' + getPerView().page.brand

            var canonical = document.querySelector('link[rel="canonical"]')
            if(canonical) canonical.href = getPerView().page.canonicalUrl

            updateMetaName("robots", getPerView().page.metaRobots)
            updateOpenGraph()

            var newLocation = path
            if (peregrineApp.isPublicFacingSite()) {
                newLocation = newLocation.replace($peregrineSiteRoot, "");
            }

            if (window.location.hash) {
                newLocation += window.location.hash;
            }

            if(firstTime) {
                history.replaceState({peregrinevue: true, path: path}, path, newLocation);
            } else {
                history.pushState({peregrinevue: true, path: path}, path, newLocation);
            }
            scroll(0, 0);
        }

        // Create the event.
        var event = document.createEvent('Event');

        // Define that the event name is 'build'.
        event.initEvent('pageRendered', true, true);

        // target can be any Element or other EventTarget.
        window.dispatchEvent(event);
    })
}

function loadContentImpl(path, firstTime, fromPopState, onPage = false) {

    log.fine('loading content for', path, firstTime, fromPopState)

    var dataUrl = pagePathToDataPath(path);
    log.fine(dataUrl)
    getPerView().status = undefined;
    if(onPage) {
        processLoadedContent(JSON.parse(document.getElementById('perPage').innerHTML), path, firstTime, fromPopState)
    } else {
        axios.get(dataUrl).then(function (response) {
            log.fine('got data for', path)
            if (response.hasOwnProperty('data') && response.data.serverSide === true) {
                document.location = `${response.data.pagePath}.html`
            } else {
                processLoadedContent(response.data, path, firstTime, fromPopState)
            }

        }).catch(function(error) {
            log.error("error getting %s %j", dataUrl, error);
        });
    }
}

function updateOpenGraph() {
    updateMetaProps('og:title', getPerView().page.ogTitle)
    updateMetaProps('og:description', getPerView().page.ogDescription)
    updateMetaProps('og:image', getPerView().page.absOgImage)
    updateMetaProps('og:url', getPerView().page.canonicalUrl)
}

function updateMetaName(key, val) {
    updateMeta(key, val, "name")
}

function updateMetaProps(key, val) {
    updateMeta(key, val, "property")
}

function updateMeta(key, val, type) {
    var meta = document.querySelector("meta[" + CSS.escape(type) + "=" +  CSS.escape(key) + "]")

    if (meta) {
      if (val) {
        meta.content = val
      } else {
        meta.parentNode.removeChild(meta);
      }
    } else {
      if (val) {
        var el = document.createElement('meta');
        el.setAttribute(type, key);
        el.content = val;
        document.getElementsByTagName('head')[0].appendChild(el);
      }
    }
}

function isAuthorModeImpl() {

    try {
        if(window && window.parent && window.frameElement && window.frameElement.attributes['data-per-mode']) {
            var mode = window.frameElement.attributes['data-per-mode'].value;
            if(mode === 'preview' || mode === 'tutorial') {
                return false
            }
        }
        if(window && window.parent && window.parent.$perAdminView && window.parent.$perAdminView.pageView) {
            return true
        }
    } catch(error) {
        // same origin
    }
    return false

}

function getAdminAppNodeImpl(path) {
    log.fine('getAdminAppState: ' + path)

    if(window && window.parent && window.parent.$perAdminApp) {
        if(window.frameElement.attributes['data-per-mode']) {
            var mode = window.frameElement.attributes['data-per-mode'].value;
            if(mode === 'tutorial') {
                return null;
            }
        }
        return window.parent.$perAdminApp.getNodeFromViewOrNull(path)
    }
    return null
}

// =============================================================================
// Renderer protocol (postMessage) - editor communication
// =============================================================================
//
// The renderer announces itself with 'renderer:ready'. An editor that speaks
// the protocol (admin v2) replies 'admin:ready' and from then on pushes model
// changes as explicit messages (page:update / component:update). The classic
// admin does not speak the protocol and keeps using the shared-object bridge
// (window.parent.$perAdminView.pageView) - both paths coexist: when the parent
// is the classic admin no protocol messages ever arrive, and when the parent
// is adminv2 there is no $perAdminView so getPerView() returns the local view.

var PROTOCOL_VERSION = '1.0'

var RENDERER_CAPABILITIES = {
    serverRefresh: false,
    reactiveUpdate: true,
    inlineEdit: true,
    dragDrop: true
}

function announceRenderer(editor) {
    editor.postMessage({
        type: 'renderer:ready',
        protocolVersion: PROTOCOL_VERSION,
        capabilities: RENDERER_CAPABILITIES,
        framework: 'vue2'
    }, window.location.origin)
    log.fine('renderer:ready sent to editor')
}

function findNodeByPath(node, path) {
    if(!node) return null
    if(node.path === path) return node
    if(node.children) {
        for(var i = 0; i < node.children.length; i++) {
            var found = findNodeByPath(node.children[i], path)
            if(found) return found
        }
    }
    return null
}

function applyPageUpdate(page) {
    // register any components the updated model references that have not been
    // seen yet (a freshly dropped component type), then swap the reactive page
    // object. The Vue instance's data IS the registered view object, so
    // assigning view.page fires Vue 2's reactive setter and the tree
    // re-renders - same assignment processLoadedContent() does on navigation.
    walkTreeAndLoad(page)
    getPerView().page = page
    log.fine('page:update applied')
}

function applyComponentUpdate(path, data) {
    var current = getPerView().page
    if(!current) return
    // structural replace: clone the page, mutate the target node in the
    // clone, reassign the page object. In-place mutation of a deep node can
    // introduce keys Vue 2 has never observed (invisible without Vue.set), so
    // the wholesale reassignment is the reliable path.
    var nextPage = JSON.parse(JSON.stringify(current))
    var node = findNodeByPath(nextPage, path)
    if(!node) {
        log.fine('component:update: no node found at', path)
        return
    }
    Object.keys(data).forEach(function(key) {
        // apply children only when the payload actually carries the subtree:
        // structural changes (add/move/delete in a container) arrive as a
        // children update and must re-render, while partial payloads without
        // children must not clobber the existing subtree
        if(key === 'children' && !Array.isArray(data.children)) return
        node[key] = data[key]
    })
    walkTreeAndLoad(node)
    getPerView().page = nextPage
    log.fine('component:update applied at', path)
}

function initEditProtocol() {
    // framed on the same origin is enough - do NOT require data-per-mode
    // here: the editor binds that attribute to reactive state and it can
    // appear only after this frame booted, which would silently skip the
    // handshake. The listener only ever acts on same-origin editor messages.
    var framed
    try {
        framed = window.parent && window.parent !== window
    } catch(error) {
        framed = false
    }
    if(!framed) return

    // the CLASSIC admin shares its reactive pageView object with this frame
    // (getView() above) and its whole vue2 editing flow is built on that.
    // Announcing the protocol would upgrade its rendererBridge to the
    // postMessage transport and abandon that battle-tested path, so when the
    // parent exposes the classic admin globals stay silent and legacy. A
    // cross-origin parent throws here, which is fine: it cannot be the
    // classic admin, so the protocol announce below proceeds.
    try {
        if(window.parent.$perAdminApp || window.parent.$perAdminView) {
            log.fine('classic admin detected - keeping legacy shared-object bridge')
            return
        }
    } catch(error) {
        // cross-origin parent: not the classic admin
    }

    var editor = window.parent

    window.addEventListener('message', function(ev) {
        if(ev.source !== editor) return
        if(ev.origin !== window.location.origin) return
        var msg = ev.data
        if(!msg || typeof msg.type !== 'string') return
        switch(msg.type) {
            case 'admin:ready':
                // ALWAYS answer the editor's probe: the editor resets its
                // transport on every iframe load event and re-probes with
                // admin:ready, and this frame's boot announce may have fired
                // before the editor listened. The editor ignores
                // renderer:ready once connected, so this cannot loop.
                announceRenderer(editor)
                break
            case 'page:update':
                if(msg.page && typeof msg.page === 'object') applyPageUpdate(msg.page)
                break
            case 'component:update':
                if(msg.path && msg.data) applyComponentUpdate(msg.path, msg.data)
                break
            case 'page:reload':
            case 'mode:change':
                window.location.reload()
                break
            default:
                // unknown message types are ignored per spec
                break
        }
    })

    announceRenderer(editor)
}

var peregrineApp = {

    registerView: function(view) {
        registerViewImpl(view)
    },

    loadContent: function(path, firstTime = false, fromPopState = false) {
        loadContentImpl(path, firstTime, fromPopState)
    },

    loadContentFrom: function(id, path, firstTime = false, fromPopState = false) {
        loadContentImpl(path, firstTime, fromPopState, true)
    },

    logger: function(name) {
        return LoggerFactory.logger(name)
    },

    loadComponent: function(name) {
        loadComponentImpl(name)
    },

    getPerVueApp: function() {
        return perVueApp
    },
    isAuthorMode: function() {
        return isAuthorModeImpl()
    },

    getView: function() {
       return getPerView()
    },

    getAdminAppNode(path) {
       return getAdminAppNodeImpl(path);
    },

    isPublicFacingSite() {
        const server = window.location.protocol + '//' + window.location.hostname;
        const domains = getPerView().page.domains || [];
        return (domains.indexOf(server) >= 0)
    }

}

// start the editor handshake as soon as the runtime is loaded (before the
// page's inline boot script runs registerView/loadContentFrom) so the editor
// hears from us no matter which side won the iframe-load race. Guarded for
// non-browser (SSR) evaluation of this bundle.
if(typeof window !== 'undefined') {
    try {
        initEditProtocol()
    } catch(error) {
        log.error('renderer protocol init failed', error)
    }
}

/**
 * you should use this object as follows:
 *
 * var $perView = {};
 * $peregrineApp.registerView($perView)
 * $peregrineApp.loadContent('/content/example/pages/index.html')
 *
 */
export default peregrineApp
