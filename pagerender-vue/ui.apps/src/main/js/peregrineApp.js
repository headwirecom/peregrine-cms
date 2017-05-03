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
    if(window && window.parent && window.parent.perAdminView && window.parent.perAdminView.pageView) {
        log.debug("getVIEW() - window.parent.perAdminView.pageView");
        return window.parent.perAdminView.pageView
    }
    return view
}

function getPerView() {
    return getView()
}

function loadComponentImpl(name) {
    if(!loadedComponents[name]) {
        log.debug('loading component', name)

        var varName = componentNameToVarName(name)
        if(window[varName]) {
            Vue.component(name, window[varName])
        }
        loadedComponents[name] = true

    } else {
        log.debug('component %s already loaded', name)
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

function processLoadedContent(data, path, firstTime) {
    walkTreeAndLoad(data)

    log.debug('first time', firstTime)
    getPerView().page = data;
    getPerView().status = 'loaded';
    if(firstTime) {
        initPeregrineApp();
    }

    if(document.location !== path) {
        log.debug("pushState : "+path);
        history.pushState({peregrinevue:true, path: path}, path, path)
    }
}

function loadContentImpl(path, firstTime) {

    log.debug('loading content for', path)

    var dataUrl = pagePathToDataPath(path);
    getPerView().status = undefined;
    axios.get(dataUrl).then(function (response) {
        log.debug('got data for', path)

        if(response.data.template) {

            var pageData = response.data

            axios.get(response.data.template+'.data.json').then(function(response) {

                var templateData = response.data
                var mergedData = merge(templateData, pageData)
                //merging nav, footer and content together with pageData
                processLoadedContent(mergedData, path, firstTime)
            }).catch(function(error) {
                log.error("error getting %s %j", dataUrl, error);
            })
        } else {
            processLoadedContent(response.data, path, firstTime)
        }

    }).catch(function(error) {
        log.error("error getting %s %j", dataUrl, error);
    });
}

var peregrineApp = {

    registerView: function(view) {
        registerViewImpl(view)
    },

    loadContent: function(path, firstTime = false) {
        loadContentImpl(path, firstTime)
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
