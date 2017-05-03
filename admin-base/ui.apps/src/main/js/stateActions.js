import { LoggerFactory } from './logger'
let log = LoggerFactory.logger('actions').setLevelDebug()

let actions = []

import selectToolsNodesPath from './stateActions/selectToolsNodesPath'
import createPage from './stateActions/createPage'
import deletePage from './stateActions/deletePage'
import createTemplate from './stateActions/createTemplate'
import createFolder from './stateActions/createFolder'
import uploadFiles from './stateActions/uploadFiles'
import editPage from './stateActions/editPage'

actions['selectToolsNodesPath'] = selectToolsNodesPath
actions['createPage'] = createPage
actions['deletePage'] = deletePage
actions['createTemplate'] = createTemplate
actions['createFolder'] = createFolder
actions['uploadFiles'] = uploadFiles
actions['editPage'] = editPage

function nopAction(me, target) {
}

export default function getStateAction(name) {

    log.fine(actions)
    if(actions[name]) {
        return actions[name]
    } else {
        log.error('stateAction', name, 'missing')
        return nopAction
    }

}