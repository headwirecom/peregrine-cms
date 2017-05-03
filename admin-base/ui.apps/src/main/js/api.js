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

    populateNodesForBrowser(path, includeParents = false) {
        return impl.populateNodesForBrowser(path, includeParents)
    }

    populateComponentDefinition(component) {
        return impl.populateComponentDefinition(component)
    }

    populateComponentDefinitionFromNode(path) {
        return impl.populateComponentDefinitionFromNode(path)
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

    createTemplate(parentPath, name) {
        return impl.createTemplate(parentPath, name)
    }

    createFolder(parentPath, name) {
        return impl.createFolder(parentPath, name)
    }

    uploadFiles(path, files) {
        return impl.uploadFiles(path, files)
    }

    savePageEdit(path, node) {
        return impl.savePageEdit(path, node)
    }

    insertNodeAt(path, component, drop) {
        return impl.insertNodeAt(path, component, drop)
    }

}

export default PerApi