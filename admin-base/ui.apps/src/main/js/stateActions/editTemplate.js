import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('editTemplate').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    me.loadContent('/content/admin/templates/edit.html/path//'+target)

}