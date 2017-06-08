import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('state').setDebugLevel()

function getClickable(node) {
    while(node.nodeName.toString() !== 'A') {
        node = node.parentNode
        if(!node) return null
    }
    return node
}

window.onclick = function(ev) {

    var currentServer = window.location.protocol+'//'+window.location.host+'/'
    log.fine("LOCATION: " , window.location.host);
    log.fine("NODENAME: " , ev.target.nodeName.toString());
    var node = getClickable(ev.target)

    if(node) {
        var toUrl = node.href
        log.fine("onClick() - "+ toUrl);
        log.fine(toUrl, currentServer)
        if(toUrl.startsWith(currentServer)) {
            ev.preventDefault()
            var gotoPath = '/'+toUrl.slice(currentServer.length)
            log.fine("gotoPath : " + gotoPath);
            $peregrineApp.loadContent(gotoPath, false)
            return false
        }
    }
}

window.onpopstate = function(e){
    if(e.state) {
        log.fine("ONPOPSTATE : " + e.state.path);
        $peregrineApp.loadContent(e.state.path, false, true)
    } else {
        log.warn(e);
        $peregrineApp.loadContent((document.location? document.location.href : null), false, true)
    }
}



export default {

    initBrowserStateHandler: function() {
        log.fine('this should init our state handler')
    }

}
