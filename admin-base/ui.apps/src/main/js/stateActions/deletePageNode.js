import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('deletePageNode').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    log.fine('deletePageNode',target)
    var api = me.getApi()
    api.deletePageNode(target.pagePath, target.pagePath+target.path).then( () => {
        let view = me.getView()
        delete view.state.editor;
        set(view, '/state/editorVisible', false)
    })

}