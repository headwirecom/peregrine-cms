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
import {LoggerFactory} from './logger'

let log = LoggerFactory.logger('api').setLevelDebug()

let impl = null

class PerApi {

    constructor(implementation) {
        log.fine('contructor')
        impl = implementation
    }

    populateUser() {
        return impl.populateUser()
    }

    populateTools() {
        return impl.populateTools()
    }

    populateToolsConfig() {
        return impl.populateToolsConfig()
    }

    populateContent(path) {
        return impl.populateContent(path)
    }

    populateComponents() {
        return impl.populateComponents()
    }

    populateTemplates() {
        return impl.populateTemplates()
    }

    populateObjects() {
        return impl.populateObjects()
    }

    populateSkeletonPages(path, target, includeParents = false) {
        return impl.populateSkeletonPages(path, target, includeParents)
    }

    populateNodesForBrowser(path, target, includeParents = false) {
        return impl.populateNodesForBrowser(path, target, includeParents)
    }

    populateComponentDefinitionFor(component) {
        return impl.populateComponentDefinitionFor(component)
    }

    populateComponentDefinitionFromNode(path) {
        return impl.populateComponentDefinitionFromNode(path)
    }

    populateExplorerDialog(path) {
        return impl.populateExplorerDialog(path)
    }


    populateObject(path, target, name) {
        return impl.populateObject(path, target, name)
    }

    populatePageView(path) {
        return impl.populatePageView(path)
    }

    populateReferencedBy(path) {
        return impl.populateReferencedBy(path)
    }

    populateTenants() {
        return impl.populateTenants()
    }

    setInitialPageEditorState() {
        return impl.setInitialPageEditorState()
    }

    populateByName(name) {
        if (name === '/admin/tools') return this.populateTools()
        if (name === '/admin/toolsConfig') return this.populateToolsConfig()
        if (name === '/admin/components') return this.populateComponents()
        return Promise.reject('populateByName for ' + name + ' is not defined')
    }

    populateI18N(language) {
        return impl.populateI18N(language)
    }

    createTenant(fromName, toName, title, tenantUserPwd, colorPalette) {
        return impl.createTenant(fromName, toName, title, tenantUserPwd, colorPalette)
    }

    createPage(parentPath, name, templatePath, title) {
        return impl.createPage(parentPath, name, templatePath, title)
    }

    createPageFromSkeletonPage(parentPath, name, skeletonPagePath) {
        return impl.createPageFromSkeletonPage(parentPath, name, skeletonPagePath)
    }

    deletePage(path) {
        return impl.deletePage(path)
    }

    deleteTenant(name, path) {
        return impl.deleteTenant(name, path)
    }

    renamePage(path, newName) {
        return impl.renamePage(path, newName)
    }

    movePage(path, to, type) {
        return impl.movePage(path, to, type)
    }

    deletePageNode(path, nodePath) {
        return impl.deletePageNode(path, nodePath)
    }

    createTemplate(parentPath, name, component, title) {
        return impl.createTemplate(parentPath, name, component, title)
    }

    moveTemplate(path, to, type) {
        return impl.moveTemplate(path, to, type)
    }

    deleteTemplate(path) {
        return impl.deleteTemplate(path)
    }

    createObject(parentPath, name, templatePath) {
        return impl.createObject(parentPath, name, templatePath)
    }

    deleteObject(path) {
        return impl.deleteObject(path)
    }

    renameObject(path, newName) {
        return impl.renameObject(path, newName)
    }

    deleteAsset(path) {
        return impl.deleteAsset(path)
    }

    renameAsset(path, newName) {
        return impl.renameAsset(path, newName)
    }

    moveAsset(path, to, type) {
        return impl.moveAsset(path, to, type)
    }

    createFolder(parentPath, name) {
        return impl.createFolder(parentPath, name)
    }

    deleteFolder(parentPath, name) {
        return impl.deleteFolder(parentPath, name)
    }

    deleteFile(path, name) {
        return impl.deleteFile(path, name)
    }

    uploadFiles(path, files, cb) {
        return impl.uploadFiles(path, files, cb)
    }

    nameAvailable(value, path) {
        return impl.nameAvailable(value, path)
    }

    fetchExternalImage(path, url, name, config) {
        return impl.fetchExternalImage(path, url, name, config)
    }

    savePageEdit(path, node) {
        return impl.savePageEdit(path, node)
    }

    saveObjectEdit(path, node) {
        return impl.saveObjectEdit(path, node)
    }

    saveAssetProperties(node) {
        return impl.saveAssetProperties(node)
    }

    insertNodeAt(path, component, drop, variation) {
        return impl.insertNodeAt(path, component, drop, variation)
    }

    insertNodeWithDataAt(path, data, drop) {
        return impl.insertNodeWithDataAt(path, data, drop)
    }

    moveNodeTo(path, component, drop) {
        return impl.moveNodeTo(path, component, drop)
    }

    replicate(path) {
        return impl.replicate(path)
    }

    getPalettes(templateName) {
        return impl.getPalettes(templateName)
    }

    tenantSetupReplication(path, withSite) {
        return impl.tenantSetupReplication(path, withSite)
    }
}

export default PerApi
