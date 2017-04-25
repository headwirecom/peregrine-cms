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
        console.info('loading vuejs component %s', name)
        var segments = name.split('-')
        for(var i = 0; i < segments.length; i++) {
            segments[i] = segments[i].charAt(0).toUpperCase() + segments[i].slice(1)
        }
        if(window['cmp'+segments.join('')]) {
            Vue.component(name, window['cmp'+segments.join('')])
        }
        loadedComponents[name] = true
    } else {
        console.info('component %s already present', name)
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

<!-- simple data loader -->
function loadContent(path, firstTime = false) {
    console.log('loading content for %s', path)
    path = path.toString()
    if(path.endsWith('.html')) {
        path = (""+path).substring(0, (""+path).indexOf('.html'))
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
            perAdminView.status = 'loaded';
            initPeregrineApp();
        } else {
            perAdminView.adminPage = response.data;
            perAdminView.status = 'loaded';
        }
        path = path + '.html'
        if(document.location !== path) {
            history.pushState({peregrinevue:true, path: path}, path, path)
        }
    }).catch(function(error) {
        console.log("error getting %s %j", dataUrl, error);
    });
}
