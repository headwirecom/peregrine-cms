import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('state').setDebugLevel()



window.onclick = function(ev) {
    // console.log(ev)
    // ev.preventDefault()
    // return false
    ev.preventDefault()


    var currentServer = window.location.protocol+'//'+window.location.host+'/'
    console.log("LOCATION: " + window.location.host);
    console.log("NODENAME: " + ev.target.nodeName.toString());
    if(ev.target.nodeName.toString() === 'A') {
        var toUrl = ev.target.href
        log.debug("onClick() - "+ toUrl);
        console.log(toUrl, currentServer)
        if(toUrl.startsWith(currentServer)) {
            var gotoPath = '/'+toUrl.slice(currentServer.length)
            log.debug("gotoPath : " + gotoPath);
            $peregrineApp.loadContent(gotoPath, false)
            return false
        }
        return false
    }
    return false
}
window.onpopstate = function(e){
  log.debug("onpopstate:" + e.state.path);
  $peregrineApp.loadContent(e.state.path, false)
}



export default {

    initBrowserStateHandler: function() {
        log.debug('this should init our state handler')
    }

}
