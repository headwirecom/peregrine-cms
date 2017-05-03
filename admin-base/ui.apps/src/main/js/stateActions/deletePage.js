import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('deletePage').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    log.fine('deletePage',target)
    var api = me.getApi()
    return api.deletePage(target)

}