function getClickable(node) {
    while(node.nodeName.toString() !== 'A') {
        node = node.parentNode
        if(!node) return null
    }
    return node
}

function getAdmin() {
    if(window && window.parent && window.parent.$perAdminApp) {
        return window.parent.$perAdminApp
    }
    return null
}

window.onclick = function(ev) {
    var currentServer = window.location.protocol+'//'+window.location.host+'/'

    var node = getClickable(ev.target)
    if(node) {
        var toUrl = node.hash   
        if(toUrl && toUrl.startsWith("#")) {

            const admin = this.getAdmin()
            if(admin) {
                admin.highlight(toUrl.substring(1))
            }

            // do nothing, it's an internal page reference
        } // else {
        //     //Dont' load new content for an href on the same page
        //     let currentUrl = window.location.href.replace(/\#\w+$/, '')
        //     if(toUrl.startsWith( currentUrl ) && toUrl.match(/\#\w+$/)) return true;
        //     if(toUrl.startsWith(currentServer)) {
        //         ev.preventDefault()
        //         var gotoPath = '/'+toUrl.slice(currentServer.length)
        //         log.fine("gotoPath : " + gotoPath);
        //         $peregrineApp.loadContent(gotoPath, false)
        //         return false
        //     }
        // }
    }
}
