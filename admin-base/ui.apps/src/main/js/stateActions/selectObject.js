import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('selectObject').setLevelDebug()

import { set } from '../utils'

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    // should actually get the data from sling and put it into the data sub node for viewing
    set(view, '/state/tools/object/show', target.selected)
    set(view, '/state/tools/object/data', me.findNodeFromPath(view.admin.nodes, target.selected))
}