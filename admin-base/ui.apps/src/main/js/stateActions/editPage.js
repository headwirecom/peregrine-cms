import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('editPage').setLevelDebug()

export default function(me, target) {

    log.fine(target)
    me.loadContent('/content/admin/pages/edit.html/path//'+target)

}