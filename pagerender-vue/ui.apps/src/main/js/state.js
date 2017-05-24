import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('state').setDebugLevel()



window.onclick = function(ev) {
    // console.log(ev)
    // ev.preventDefault()
    // return false
    ev.preventDefault()


    var currentServer = window.location.protocol+'//'+window.location.host+'/'
    log.fine("LOCATION: " , window.location.host);
    log.fine("NODENAME: " , ev.target.nodeName.toString());
    if(ev.target.nodeName.toString() === 'A') {
        var toUrl = ev.target.href
        log.fine("onClick() - "+ toUrl);
        log.fine(toUrl, currentServer)
        if(toUrl.startsWith(currentServer)) {
            var gotoPath = '/'+toUrl.slice(currentServer.length)
            log.fine("gotoPath : " + gotoPath);
            $peregrineApp.loadContent(gotoPath, false)
            return false
        }
        return false
    }
    return false
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
