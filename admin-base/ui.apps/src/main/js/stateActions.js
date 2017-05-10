import { LoggerFactory } from './logger'
let log = LoggerFactory.logger('actions').setLevelDebug()

let actions = []

import selectToolsNodesPath from './stateActions/selectToolsNodesPath'
import createPage from './stateActions/createPage'
import createPageWizard from './stateActions/createPageWizard'
import deletePage from './stateActions/deletePage'
import createTemplate from './stateActions/createTemplate'
import createFolder from './stateActions/createFolder'
import uploadFiles from './stateActions/uploadFiles'
import editPage from './stateActions/editPage'
import editComponent from './stateActions/editComponent'
import savePageEdit from './stateActions/savePageEdit'
import deletePageNode from './stateActions/deletePageNode'
import cancelPageEdit from './stateActions/cancelPageEdit'
import addComponentToPath from './stateActions/addComponentToPath'
import selectObject from './stateActions/selectObject'
import unselectObject from './stateActions/unselectObject'
import saveObjectEdit from './stateActions/saveObjectEdit'
import createObject from './stateActions/createObject'
import selectAsset from './stateActions/selectAsset'
import unselectAsset from './stateActions/unselectAsset'

actions['selectToolsNodesPath'] = selectToolsNodesPath
actions['createPageWizard'] = createPageWizard
actions['createPage'] = createPage
actions['deletePage'] = deletePage
actions['createTemplate'] = createTemplate
actions['createFolder'] = createFolder
actions['uploadFiles'] = uploadFiles
actions['editPage'] = editPage
actions['editComponent'] = editComponent
actions['savePageEdit'] = savePageEdit
actions['addComponentToPath'] = addComponentToPath
actions['deletePageNode'] = deletePageNode
actions['cancelPageEdit'] = cancelPageEdit
actions['selectObject'] = selectObject
actions['unselectObject'] = unselectObject
actions['saveObjectEdit'] = saveObjectEdit
actions['createObject'] = createObject
actions['selectAsset'] = selectAsset
actions['unselectAsset'] = unselectAsset

function noopAction(me, target) {
    log.error('state action noop with target:', target)
}

export default function getStateAction(name) {

    log.fine(actions)
    if(actions[name]) {
        return actions[name]
    } else {
        log.error('stateAction', name, 'missing')
        return noopAction
    }

}