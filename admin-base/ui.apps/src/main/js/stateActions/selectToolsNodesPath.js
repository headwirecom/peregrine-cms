import { LoggerFactory } from '../logger'
let logger = LoggerFactory.logger('selectToolsNodesPath').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    set(me.getView(), target.path, target.selected)
    me.getApi().populateNodesForBrowser(target.selected).then( () => {
        let path = document.location.pathname
        let html = path.indexOf('.html')
        let newPath = path.slice(0,html) + '.html/path//'+target.selected
        history.pushState({peregrinevue:true, path: newPath}, newPath, newPath)
    })

}