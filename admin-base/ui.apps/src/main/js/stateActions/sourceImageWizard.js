import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('sourceImageWizard').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    me.loadContent('/content/admin/assets/source.html/path//'+target)

}