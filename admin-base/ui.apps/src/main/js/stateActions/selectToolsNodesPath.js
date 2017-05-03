import { LoggerFactory } from '../logger'
let logger = LoggerFactory.logger('selectToolsNodesPath').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    set(me.getView(), target.path, target.selected)
    me.getApi().populateNodesForBrowser(target.selected)

}