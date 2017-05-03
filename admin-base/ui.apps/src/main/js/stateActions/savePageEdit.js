import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('savePageEdit').setLevelFine()

import { set } from '../utils'

export default function(me, target) {

    log.fine(target)

    let view = me.getView()
    let nodeToSave = me.findNodeFromPath(view.pageView.page, target.path)
    me.getApi().savePageEdit(view.pageView.path, nodeToSave).then( () => {
        delete view.state.editor;
        set(view, '/state/editorVisible', false)
    })
    // me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then( (name) => {
    //         log.fine('component name is', name)
    //         set(view, '/state/editor/component', name)
    //         set(view, '/state/editor/path', target)
    //         set(view, '/state/editorVisible', true)
    //         set(view, '/state/rightPanelVisible', true)
    //     }
    // )
}