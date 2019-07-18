/*-
 * #%L
 * admin base - UI Apps
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
import { LoggerFactory } from './logger'
let log = LoggerFactory.logger('actions').setLevelDebug()

let actions = []

import selectToolsNodesPath from './stateActions/selectToolsNodesPath'
import createPage from './stateActions/createPage'
import createPageWizard from './stateActions/createPageWizard'
import createSiteWizard from './stateActions/createSiteWizard'
import createSite from './stateActions/createSite'
import deletePage from './stateActions/deletePage'
import renamePage from './stateActions/renamePage'
import movePage from './stateActions/movePage'
import createTemplate from './stateActions/createTemplate'
import createTemplateWizard from './stateActions/createTemplateWizard'
import sourceImageWizard from './stateActions/sourceImageWizard'
import fetchExternalAsset from './stateActions/fetchExternalAsset'
import createFolder from './stateActions/createFolder'
import deleteFolder from './stateActions/deleteFolder'
import uploadFiles from './stateActions/uploadFiles'
import editPage from './stateActions/editPage'
import editTemplate from './stateActions/editTemplate'
import editComponent from './stateActions/editComponent'
import savePageEdit from './stateActions/savePageEdit'
import deletePageNode from './stateActions/deletePageNode'
import cancelPageEdit from './stateActions/cancelPageEdit'
import addComponentToPath from './stateActions/addComponentToPath'
import moveComponentToPath from './stateActions/moveComponentToPath'
import selectObject from './stateActions/selectObject'
import editObject from './stateActions/editObject'
import unselectObject from './stateActions/unselectObject'
import saveObjectEdit from './stateActions/saveObjectEdit'
import saveAssetProperties from './stateActions/saveAssetProperties'
import deleteObject from './stateActions/deleteObject'
import createObject from './stateActions/createObject'
import createObjectWizard from './stateActions/createObjectWizard'
import selectAsset from './stateActions/selectAsset'
import unselectAsset from './stateActions/unselectAsset'
import unselectPage from './stateActions/unselectPage'
import unselectTemplate from './stateActions/unselectTemplate'
import deleteAsset from './stateActions/deleteAsset'
import renameAsset from './stateActions/renameAsset'
import moveAsset from './stateActions/moveAsset'
import showPageInfo from './stateActions/showPageInfo'
import editPreview from './stateActions/editPreview'
import createAssetFolder from './stateActions/createAssetFolder'
import createObjectFolder from './stateActions/createObjectFolder'
import createAssetFolderWizard from './stateActions/createAssetFolderWizard'
import createObjectFolderWizard from './stateActions/createObjectFolderWizard'
import savePageProperties from './stateActions/savePageProperties'
import replicate from './stateActions/replicate'
import moveTemplate from './stateActions/moveTemplate'
import renameObject from './stateActions/renameObject'
import moveObject from './stateActions/moveObject'

actions['selectToolsNodesPath'] = selectToolsNodesPath
actions['createSiteWizard'] = createSiteWizard
actions['createSite'] = createSite
actions['createPageWizard'] = createPageWizard
actions['createPage'] = createPage
actions['deletePage'] = deletePage
actions['renamePage'] = renamePage
actions['movePage'] = movePage
actions['createTemplate'] = createTemplate
actions['createTemplateWizard'] = createTemplateWizard
actions['createFolder'] = createFolder
actions['deleteFolder'] = deleteFolder
actions['uploadFiles'] = uploadFiles
actions['sourceImageWizard'] = sourceImageWizard
actions['fetchExternalAsset'] = fetchExternalAsset
actions['editPage'] = editPage
actions['editTemplate'] = editTemplate
actions['editComponent'] = editComponent
actions['savePageEdit'] = savePageEdit
actions['addComponentToPath'] = addComponentToPath
actions['moveComponentToPath'] = moveComponentToPath
actions['deletePageNode'] = deletePageNode
actions['cancelPageEdit'] = cancelPageEdit
actions['selectObject'] = selectObject
actions['editObject'] = editObject
actions['unselectObject'] = unselectObject
actions['saveObjectEdit'] = saveObjectEdit
actions['saveAssetProperties'] = saveAssetProperties
actions['deleteObject'] = deleteObject
actions['createObject'] = createObject
actions['createObjectWizard'] = createObjectWizard
actions['selectAsset'] = selectAsset
actions['showPageInfo'] = showPageInfo
actions['unselectAsset'] = unselectAsset
actions['unselectPage'] = unselectPage
actions['unselectTemplate'] = unselectTemplate
actions['deleteAsset'] = deleteAsset
actions['renameAsset'] = renameAsset
actions['moveAsset'] = moveAsset
actions['editPreview'] = editPreview
actions['createAssetFolder'] = createAssetFolder
actions['createAssetFolderWizard'] = createAssetFolderWizard
actions['createObjectFolder'] = createObjectFolder
actions['createObjectFolderWizard'] = createObjectFolderWizard
actions['renameObject'] = renameObject
actions['savePageProperties'] = savePageProperties
actions['replicate'] = replicate
actions['moveObject'] = moveObject
actions['moveTemplate'] = moveTemplate

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
