import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('sourceImageWizard').setLevelDebug()

export default function(me, target) {

    log.fine(target)

    var api = me.getApi()
    api.fetchExternalImage(target.path, target.url, target.name).then( () => {
        me.loadContent('/content/admin/assets.html/path//'+target.path)
    })


}