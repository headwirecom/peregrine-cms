import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('selectAsset').setLevelDebug()

import { set } from '../utils'

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    set(view, '/state/tools/object/show', target.selected)
}