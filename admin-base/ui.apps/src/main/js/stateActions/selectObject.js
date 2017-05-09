import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('selectObject').setLevelDebug()

import { set } from '../utils'

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    me.getApi().populateObject(target.selected, '/state/tools/object', 'data').then( () => {
        set(view, '/state/tools/object/show', target.selected)
    })
}