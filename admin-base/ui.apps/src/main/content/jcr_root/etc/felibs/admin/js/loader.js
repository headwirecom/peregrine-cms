<!-- hook up routing of links -->
//window.onclick = function(ev) {
//    var currentServer = window.location.protocol+'//'+window.location.host+'/'
//    if(ev.target.toString().startsWith(currentServer)) {
//        gotoPath = '/'+ev.target.toString().slice(currentServer.length)
//        loadContent(gotoPath)
//        return false;
//    }
//}
//
//window.onpopstate = function(ev) {
//
//    console.log("%j", ev)
//    if(ev.state && ev.state.peregrinevue) {
//        loadContent(ev.state.path, false)
//    } else {
//        loadContent(document.location, false)
//    }
//
//}

<!-- variable to hold view of page -->
var perAdminView = {
    state: {},
    pageView: {},
    adminPage: {},
    admin: {},
    pages: {
        children: []
    }
};
var loadedComponents = {};
var peregrineAdminApp;

<!-- initialization of peregrine vuejs renderer -->
function initPeregrineApp() {

    // hack to check keys in pending
    var loading = false
//    for(key in pending) {
//        loading = true
//    }

    if(!loading && !peregrineAdminApp) {
        console.log('initialize vue')
        peregrineAdminApp = new Vue({
            el: '#peregrine-adminapp',
            data: perAdminView
        });
    }
}

<!-- dynamic component initializer -->
function loadComponent(name) {
    if(!loadedComponents[name]) {
//        console.info('loading vuejs component %s', name)
        var segments = name.split('-')
        for(var i = 0; i < segments.length; i++) {
            segments[i] = segments[i].charAt(0).toUpperCase() + segments[i].slice(1)
        }
        if(window['cmp'+segments.join('')]) {
            Vue.component(name, window['cmp'+segments.join('')])
        }
        loadedComponents[name] = true
    } else {
//        console.info('component %s already present', name)
    }
}

<!-- tree walker -->
function walkTreeAndLoad(node) {

    if(node.component) loadComponent(node.component)
    if(node.source) loadData(node.source)

    if(node.children) {
        node.children.forEach(function (child) {
            walkTreeAndLoad(child)
        })
    }

}

function makePathInfo(path) {

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

    return { path: pathPart, suffix: suffixPath , suffixParams: suffixParams }
}

function suffixParamsToModel(suffixParams, mappers) {

    if(mappers) {
        for(var i = 0; i < mappers.length; i+=2) {

            var paramName = mappers[i]
            var paramPath = mappers[i+1]
            var paramValue = suffixParams[paramName]

            if(paramValue) {
                console.log('set',paramPath,'to',paramValue)
                perHelperSet(perAdminView, paramPath, paramValue)
            }
        }
    }

}

<!-- simple data loader -->
function loadContent(initialPath, firstTime = false) {
    console.log('loading content for %s', initialPath)
    var pathInfo = makePathInfo(initialPath.toString())
    path = pathInfo.path
    if(path.endsWith('.html')) {
        path = path.substring(0, (""+path).indexOf('.html'))
    }
    var dataUrl = path + '.data.json';
    perAdminView.status = undefined;
    axios.get(dataUrl).then(function (response) {
        console.log('got data for %s', path)
        walkTreeAndLoad(response.data)



        loadComponent('admin-components-iconaction')
        loadComponent('admin-components-action')

        if(firstTime) {
            perAdminView.adminPage = response.data;

            suffixParamsToModel(pathInfo.suffixParams, perAdminView.adminPage.suffixToParameter)

            perAdminView.status = 'loaded';
            initPeregrineApp();
        } else {
            perAdminView.adminPage = response.data;
            perAdminView.status = 'loaded';
        }
        if(document.location !== initialPath) {
            history.pushState({peregrinevue:true, path: initialPath}, initialPath, initialPath)
        }
    }).catch(function(error) {
        console.log("error getting %s %j", dataUrl, error);
    });
}
