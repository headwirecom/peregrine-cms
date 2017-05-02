import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('createFolder').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    log.fine(target)
    var api = me.getApi()
    return api.createFolder(target.parent, target.name)

}