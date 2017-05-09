import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('createPage').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    log.fine(target)
    var api = me.getApi()
    api.createPage(target.parent, target.name, target.template).then( () => {
        me.loadContent('/content/admin/pages.html/path//'+ target.parent)
    })

}