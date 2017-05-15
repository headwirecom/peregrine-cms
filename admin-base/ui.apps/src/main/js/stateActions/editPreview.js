import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('editPreview').setLevelDebug()

import { set } from '../utils'

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    set(view, '/state/tools/workspace/view', target)
}