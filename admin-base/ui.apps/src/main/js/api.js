import { LoggerFactory } from './logger'
let logger = LoggerFactory.logger('api').setLevelDebug()

let impl = null

class PerApi {

    constructor(implementation) {
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

    populateNodesForBrowser(path, includeParents = false) {
        return impl.populateNodesForBrowser(path, includeParents)
    }

    populateComponentDefinition(component) {
        return impl.populateComponentDefinition(component)
    }

    populateComponentDefinitionFromNode(path) {
        return impl.populateComponentDefinitionFromNode(path)
    }

    populateObject(path, target, name) {
        return impl.populateObject(path, target, name)
    }

    populatePageView(path) {
        return impl.populatePageView(path)
    }

    setInitialPageEditorState() {
        return impl.setInitialPageEditorState()
    }

    populateByName(name) {
        if(name === '/admin/tools') return this.populateTools()
        if(name === '/admin/toolsConfig') return this.populateToolsConfig()
        if(name === '/admin/components') return this.populateComponents()
        return Promise.reject('populateByName for '+name+' is not defined')
    }

    createPage(parentPath, name, templatePath) {
        return impl.createPage(parentPath, name, templatePath)
    }

    deletePage(path) {
        return impl.deletePage(path)
    }

    deletePageNode(path, nodePath) {
        return impl.deletePageNode(path, nodePath)
    }

    createTemplate(parentPath, name) {
        return impl.createTemplate(parentPath, name)
    }

    createObject(parentPath, name, templatePath) {
        return impl.createObject(parentPath, name, templatePath)
    }

    createFolder(parentPath, name) {
        return impl.createFolder(parentPath, name)
    }

    uploadFiles(path, files, cb) {
        return impl.uploadFiles(path, files, cb)
    }

    fetchExternalImage(path, url, name) {
        return impl.fetchExternalImage(path, url, name)
    }

    savePageEdit(path, node) {
        return impl.savePageEdit(path, node)
    }

    saveObjectEdit(path, node) {
        return impl.saveObjectEdit(path, node)
    }

    insertNodeAt(path, component, drop) {
        return impl.insertNodeAt(path, component, drop)
    }

    insertNodeWithDataAt(path, data, drop) {
        return impl.insertNodeWithDataAt(path, data, drop)
    }

    moveNodeTo(path, component, drop) {
        return impl.moveNodeTo(path, component, drop)
    }
}

export default PerApi