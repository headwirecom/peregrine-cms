import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('addComponentToPath').setLevelDebug()

import { parentPath } from '../utils'

export default function(me, target) {

    log.fine('addComponentToPath()',target)

    let view = me.getView()

    let targetNode = null
    if(target.drop === 'into') {
        targetNode = me.findNodeFromPath(view.pageView.page, target.path)
    } else if(target.drop === 'before' || target.drop === 'after') {
        var path = parentPath(target.path)
        log.fine('insert at', path.parentPath, target.drop, path.name)
        targetNode = me.findNodeFromPath(view.pageView.page, path.parentPath)
    } else {
        log.error('addComponentToPath() target.drop not in allowed values - value was', target.drop)
    }
    if(targetNode) {
        me.getApi().insertNodeAt(target.pagePath+targetNode.path, target.component, target.drop)
            .then( (data) => {
                if(target.drop === 'into') {
                    Vue.set(targetNode, 'children', data.children)
                }
                log.fine(data)
            })
    }

    // me.getApi().savePageEdit(view.pageView.path, nodeToSave).then( () => {
    //     delete view.state.editor;
    //     set(view, '/state/editorVisible', false)
    // })
    // me.getApi().populateComponentDefinitionFromNode(view.pageView.path+target).then( (name) => {
    //         log.fine('component name is', name)
    //         set(view, '/state/editor/component', name)
    //         set(view, '/state/editor/path', target)
    //         set(view, '/state/editorVisible', true)
    //         set(view, '/state/rightPanelVisible', true)
    //     }
    // )
}