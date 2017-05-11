import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('editComponent').setLevelDebug()

import { set } from '../utils'

function bringUpEditor(me, view, target) {

    me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then( (name) => {
            log.fine('component name is', name)
            set(view, '/state/editor/component', name)
            set(view, '/state/editor/path', target)
            set(view, '/state/editorVisible', true)
            set(view, '/state/rightPanelVisible', true)
        }
    )
}

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    if(view.state.editorVisible) {
        me.getApi().populatePageView(view.pageView.path).then( () => {
            bringUpEditor(me, view, target)
        })
    } else {
        bringUpEditor(me, view, target)
    }


}