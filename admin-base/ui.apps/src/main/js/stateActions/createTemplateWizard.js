import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('createTemplateWizard').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    me.loadContent('/content/admin/templates/create.html/path//'+target)

}