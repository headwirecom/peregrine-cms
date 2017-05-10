import { LoggerFactory } from '../logger'
let log = LoggerFactory.logger('moveComponentToPath').setLevelDebug()

import { parentPath } from '../utils'

export default function(me, target) {

    log.fine('moveComponentToPath()',target)

    let view = me.getView()

    let targetNodeUpdate = null
    if(target.drop === 'into') {
        targetNodeUpdate = me.findNodeFromPath(view.pageView.page, target.path)
    } else if(target.drop === 'before' || target.drop === 'after') {
        var path = parentPath(target.path)
        targetNodeUpdate = me.findNodeFromPath(view.pageView.page, path.parentPath)
    } else {
        log.error('addComponentToPath() target.drop not in allowed values - value was', target.drop)
    }


    me.getApi().moveNodeTo(target.pagePath+target.path, target.pagePath+target.component, target.drop)
        .then( (data) => {
                if(target.drop === 'into') {
                    Vue.set(targetNodeUpdate, 'children', data.children)
                }
                else if(target.drop === 'before' || target.drop === 'after')
                {
                    Vue.set(targetNodeUpdate, 'children', data.children)
                }

                var node = me.findNodeFromPath(view.pageView.page, target.component)
                var parent = me.findNodeFromPath(view.pageView.page, parentPath(target.component).parentPath)
                console.log(node)
                console.log(parent)
                let index = parent.children.indexOf(node)
                parent.children.splice(index, 1)
            })
//    }

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