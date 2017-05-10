import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('createObjectWizard').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    me.loadContent('/content/admin/objects/create.html/path//'+target)

}