import { LoggerFactory } from '../logger'
let logger = LoggerFactory.logger('selectToolsNodesPath').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {
    let view = me.getView()
    set(view, '/state/tools/page', null)

    me.getApi().populateNodesForBrowser(target.selected).then( () => {
    		set(me.getView(), target.path, target.selected)
        let path = document.location.pathname
        let html = path.indexOf('.html')
        let newPath = path.slice(0,html) + '.html/path//'+target.selected
        history.pushState({peregrinevue:true, path: newPath}, newPath, newPath)
    })

}