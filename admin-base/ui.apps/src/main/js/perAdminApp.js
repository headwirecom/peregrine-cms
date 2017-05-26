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

/** dynamic component initializer **/
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
/** tree walker **/
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

/** simple data loader **/
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
        cb()
    })
    set(view, '/state/notification/isVisible', true)
}

function pathBrowserImpl(root, cb) {
    api.populateNodesForBrowser('/content/assets')
    set(view, '/state/pathbrowser/root', root)
    set(view, '/state/pathbrowser/isVisible', false)
    set(view, '/state/pathbrowser/methods', {
        onShow: function(){ set(view, '/state/pathbrowser/isVisible', true) },
        onHide: function(){ set(view, '/state/pathbrowser/isVisible', false) },
        setItemPath: cb
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

var PerAdminApp = {

    init(perAdminAppView) {
        view = perAdminAppView
        api = new PeregrineApi(new PerAdminImpl(PerAdminApp))
    },

    getLoggers() {
        return LoggerFactory.getLoggers()
    },

    getLogger(name) {
        if(!name) return logger
        logger.fine('getting logger for',name)
        return LoggerFactory.logger(name)
    },

    getApi() {
        return api
    },

    getView() {
        return view
    },

    loadContent(path, firstTime = false) {
        loadContentImpl(path, firstTime)
    },

    loadComponent(name) {
        loadComponentImpl(name)
    },

    getComponentByName(name) {
        return getComponentByNameImpl(name)
    },

    action(component, command, target) {
        actionImpl(component, command, target)
    },

    stateAction(name, target) {
        stateActionImpl(name, target)
    },

    getNodeFrom(node, path) {
        return getNodeFromImpl(node, path)
    },

    getNodeFromView(path) {
        return getNodeFromImpl(this.getView(), path)
    },

    getNodeFromViewOrNull(path) {
        return getNodeFromOrNullImpl(this.getView(), path)
    },

    getNodeFromViewWithDefault(path, value) {
        return getNodeFromWithDefaultImpl(this.getView(), path, value)
    },

    findNodeFromPath(node, path) {
        return findNodeFromPathImpl(node, path)
    },

    getApp() {
        return app;
    },

    notifyUser(title, message, cb) {
        notifyUserImpl(title, message, cb)
    },

    pathBrowser(rootPath, cb) {
        pathBrowserImpl(rootPath, cb)
    },

    isPreviewMode() {
        return isPreviewModeImpl()
    },

    getOSBrowser(){
        return getOSBrowserImpl()
    }
}

export default PerAdminApp
