import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('createPageWizard').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    me.loadContent('/content/admin/pages/create.html/path//'+target)

}