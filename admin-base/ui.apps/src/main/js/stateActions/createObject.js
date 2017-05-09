import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('createObject').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    log.fine(target)
    var api = me.getApi()
    return api.createObject(target.parent, target.name, target.template)

}