import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('state').setDebugLevel()


window.onclick = function(ev) {
    // console.log(ev)
    // ev.preventDefault()
    // return false
    ev.preventDefault()

    var currentServer = window.location.protocol+'//'+window.location.host+'/'
    console.log(ev.target.nodeName.toString())
    if(ev.target.nodeName.toString() === 'A') {
        var toUrl = ev.target.href
        console.log(toUrl, currentServer)
        if(toUrl.startsWith(currentServer)) {
            var gotoPath = '/'+toUrl.slice(currentServer.length)
            $peregrineApp.loadContent(gotoPath, false)
            return false
        }
        return false
    }
    return false
}


export default {

    initBrowserStateHandler: function() {
        log.debug('this should init our state handler')
    }

}
