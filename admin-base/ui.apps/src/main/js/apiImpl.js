var axios = require('axios')

import { LoggerFactory } from './logger'
let logger = LoggerFactory.logger('apiImpl').setLevelDebug()

const API_BASE = 'http://admin:admin@localhost:8080/api'

let callbacks

function fetch(path) {

    logger.debug('fetch() ', path)
    return axios.get(API_BASE+path).then( (response) => {
        return new Promise( (resolve, reject) => {
            resolve(response.data)
        })
    }).catch( (error) => { logger.error('request to',
            error.response.request.path, 'failed')
            throw error
        })

}

function getOrCreate(obj, path) {

    var segments = path.split('/').slice(1).reverse()

    while(segments.length > 0) {
        var segment = segments.pop()
        if(!obj[segment]) {
            obj[segment] = {}
        }
        obj = obj[segment]
    }

    return obj
}

function populateView(path, name, data) {

    return new Promise( (resolve, reject) => {
        var obj = getOrCreate(callbacks.getView(), path)
        obj[name] = data
        resolve()
    })

}


class PerAdminImpl {

    constructor(cb) {
        callbacks = cb
    }

    populateTools() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/list.json/tools')
                .then( (data) => populateView('/admin', 'tools', data) )
                .then(() => resolve() )
                .catch( (error) => {
                    logger.error('call populateTools() failed')
                    reject(error)
                })
        })
    }

    populateToolsConfig() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/list/tools/config')
                .then( (data) => populateView('/admin', 'toolsConfig', data) )
                .then(() => resolve() )
        })
    }

    populateComponents() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/components')
                .then( (data) => populateView('/admin', 'components', data) )
                .then( () => resolve() )
        })
    }

    populateNodesForBrowser(path, includeParents = false) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/nodes/path//'+path+'//includeParents/'+includeParents)
                .then( (data) => populateViw('/admin', 'pages', data) )
                .then(() => resolve() )
        })
    }

    populateComponentDefinition(component) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/components/'+component)
                .then( (data) => populateView('/admin/componentDefinitions', component, data) )
                .then( () => resolve() )
        })
    }

    populateComponentDefinitionFromNode(path) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/componentDefinition/path//'+path)
                .then( (data) => populateView('/admin/componentDefinitions', data.name, data.config) )
                .then( () => resolve() )
        })
    }

}

export default PerAdminImpl