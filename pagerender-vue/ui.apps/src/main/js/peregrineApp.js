import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('peregrineApp').setDebugLevel()

import state from './state.js'
import merge from './merge.js'
import { pagePathToDataPath, componentNameToVarName } from './util.js'


let view
let loadedComponents = []

let perVueApp = null

function initPeregrineApp() {

    perVueApp = new Vue({
        el: '#peregrine-app',
        data: getPerView()
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
            Vue.component(name, window[varName])
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

function processLoadedContent(data, path, firstTime, fromPopState) {
    walkTreeAndLoad(data)

    log.fine('first time', firstTime)
    getPerView().page = data;
    getPerView().path = path.slice(0, path.indexOf('.html'));
    getPerView().status = 'loaded';
    if(firstTime) {
        initPeregrineApp();
    }

    if(document.location !== path && !fromPopState && !firstTime) {
        log.fine("PUSHSTATE : "+path);
        document.title = getPerView().page.title
        history.pushState({peregrinevue:true, path: path}, path, path)
    }
}

function loadContentImpl(path, firstTime, fromPopState) {

    log.fine('loading content for', path, firstTime, fromPopState)

    var dataUrl = pagePathToDataPath(path);
    log.fine(dataUrl)
    getPerView().status = undefined;
    axios.get(dataUrl).then(function (response) {
        log.fine('got data for', path)

        // if(response.data.template) {
        //
        //     var pageData = response.data
        //
        //     axios.get(response.data.template+'.data.json').then(function(response) {
        //
        //         var templateData = response.data
        //         var mergedData = merge(templateData, pageData)
        //         //merging nav, footer and content together with pageData
        //         processLoadedContent(mergedData, path, firstTime, fromPopState)
        //     }).catch(function(error) {
        //         log.error("error getting %s %j", dataUrl, error);
        //     })
        // } else {
        processLoadedContent(response.data, path, firstTime, fromPopState)
        // }

    }).catch(function(error) {
        log.error("error getting %s %j", dataUrl, error);
    });
}

var peregrineApp = {

    registerView: function(view) {
        registerViewImpl(view)
    },

    loadContent: function(path, firstTime = false, fromPopState = false) {
        loadContentImpl(path, firstTime, fromPopState)
    },

    logger: function(name) {
        return logger.logger(name)
    },

    loadComponent: function(name) {
        loadComponentImpl(name)
    },

    getPerVueApp: function() {
        return perVueApp
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
