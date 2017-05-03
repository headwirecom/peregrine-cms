import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('uploadFiles').setLevelDebug()

export default function(me, target) {

    log.fine(target)
    var api = me.getApi()
    return api.uploadFiles(target.path, target.files)
//    return Promise.resolve()

}