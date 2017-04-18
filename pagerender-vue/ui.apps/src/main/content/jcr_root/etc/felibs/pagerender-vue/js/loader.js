<!-- hook up routing of links -->
window.onclick = function(ev) {
    var currentServer = window.location.protocol+'//'+window.location.host+'/'
    if(ev.target.toString().startsWith(currentServer)) {
        gotoPath = '/'+ev.target.toString().slice(currentServer.length)
        loadContent(gotoPath)
        return false;
    }
}

window.onpopstate = function(ev) {

    console.log("%j", ev)
    if(ev.state && ev.state.peregrinevue) {
        loadContent(ev.state.path, false)
    } else {
        loadContent(document.location, false)
    }

}

<!-- variable to hold view of page -->
var perView = {};
var loadedComponents = {};
var peregrineApp;

<!-- initialization of peregrine vuejs renderer -->
function initPeregrineApp() {
    peregrineApp = new Vue({
        el: '#peregrine-app',
        data: perView
    });
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
    if(node.children) {
        node.children.forEach(function (child) {
            walkTreeAndLoad(child)
        })
    }

}

<!-- simple data loader -->
function loadContent(path, firstTime = false) {
    console.log('loading content for %s', path)
    var dataUrl = (""+path).substring(0, (""+path).indexOf('.html')) + '.data.json';
    perView.status = undefined;
    axios.get(dataUrl).then(function (response) {
        console.log('got data for %s', path)
        walkTreeAndLoad(response.data)
        if(firstTime) {
            perView.page = response.data;
            perView.status = 'loaded';
            initPeregrineApp();
        } else {
            perView.page = response.data;
            perView.status = 'loaded';
        }
        if(document.location !== path) {
            history.pushState({peregrinevue:true, path: path}, path, path)
        }
    }).catch(function(error) {
        console.log("error getting %s %j", dataUrl, error);
    });
}
