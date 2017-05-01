import { LoggerFactory } from './logger'
let logger = LoggerFactory.logger('api').setLevelDebug()

let impl = null

class PerApi {

    constructor(implementation) {
        impl = implementation
    }

    populateTools() {
        return impl.populateTools()
    }

    populateToolsConfig() {
        return impl.populateToolsConfig()
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
}

export default PerApi