import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('cancelPageEdit').setLevelDebug()

import {set} from '../utils'

export default function(me, target) {

    var api = me.getApi()
    api.populatePageView(target.pagePath).then( () => {
        let view = me.getView()
        delete view.state.editor;
        set(view, '/state/editorVisible', false)
    })

}