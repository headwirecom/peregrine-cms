import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('addComponentToPath').setLevelDebug()

import { parentPath } from '../utils'

export default function(me, target) {

    log.fine('addComponentToPath()',target)

    let view = me.getView()

    // resolve path to component name
    let componentName = target.component.split('/').slice(2).join('-')
    log.fine('load',componentName, 'into edit view (make sure it is available')
    document.getElementById('editview').contentWindow.$peregrineApp.loadComponent(componentName)

    let targetNode = null
    let targetNodeUpdate = null
    if(target.drop === 'into') {
        targetNode = me.findNodeFromPath(view.pageView.page, target.path)
        targetNodeUpdate = targetNode
    } else if(target.drop === 'before' || target.drop === 'after') {
        var path = parentPath(target.path)
        targetNodeUpdate = me.findNodeFromPath(view.pageView.page, path.parentPath)
        targetNode = me.findNodeFromPath(view.pageView.page, target.path)
    } else {
        log.error('addComponentToPath() target.drop not in allowed values - value was', target.drop)
    }
    if(targetNode) {
        me.getApi().insertNodeAt(target.pagePath+targetNode.path, target.component, target.drop)
            .then( (data) => {
                if(target.drop === 'into') {
                    Vue.set(targetNodeUpdate, 'children', data.children)
                }
                else if(target.drop === 'before' || target.drop === 'after')
                {
                    Vue.set(targetNodeUpdate, 'children', data.children)
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