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
// var axios = require('axios')

import { LoggerFactory } from './logger'
let logger = LoggerFactory.logger('apiImpl').setLevelDebug()

import { stripNulls} from './utils'

const API_BASE = '/perapi'
const postConfig = {
    withCredentials: true
}

let callbacks

function fetch(path) {

    logger.fine('Fetch ', path)
    return axios.get(API_BASE+path).then( (response) => {
        return new Promise( (resolve, reject) => {

            // Fix for IE11
            if((typeof response.data === 'string' && response.data.startsWith("<!DOCTYPE")) || (response.request && response.request.responseURL && response.request.responseURL.indexOf('/system/sling/form/login') >= 0)) {
                window.location = '/system/sling/form/login'
                reject('need to authenticate')
            }
            resolve(response.data)
        })
    }).catch( (error) => { logger.error('Fetch request to',
            path, 'failed')
            throw error
        })

}

function update(path) {

    logger.fine('Update, path: ', path)
    return axios.post(API_BASE+path, null, postConfig)
        .then( (response) => {
            return new Promise( (resolve, reject) => {
                logger.fine('Update, response data: ' + response.data)
                resolve(response.data)
            })
        })
        .catch( (error) => {
            logger.error('Update request to', error.response.request.path, 'failed')
            throw error
        })
}

function updateWithForm(path, data) {

    logger.fine('Update with Form, path: ' + path + ', data: ' + data)
    return axios.post(API_BASE+path, data, postConfig)
        .then( (response) => {
            return new Promise( (resolve, reject) => {
                logger.fine('Update with Form, response data: ' + response.data)
                resolve(response.data)
            })
        })
        .catch( (error) => {
            logger.error('Update with Form request to', error.response.request.path, 'failed')
            throw error
        })
}

function updateWithFormAndConfig(path, data, config) {
    //AS TODO: How to merge config into postConfig or the other way around?
    // config.withCredentials: true
    logger.fine('Update with Form and Config, path: ' + path + ', data: ' + data)
    return axios.post(API_BASE+path, data, config)
        .then( (response) => {
            return new Promise( (resolve, reject) => {
                logger.fine('Update with Form and Config, response data: ' + response.data)
                resolve(response.data)
            })
        })
        .catch( (error) => {
            logger.error('Update with Form and Config request to', error.response.request.path, 'failed')
            throw error
        })
}

function getOrCreate(obj, path) {

    if(path === '/') {
        // do nothing, requesting root
    }
    else {
        var segments = path.split('/').slice(1).reverse()

        while(segments.length > 0) {
            var segment = segments.pop()
            if(!obj[segment]) {
                Vue.set(obj, segment, {})
//                obj[segment] = {}
            }
            obj = obj[segment]
        }
    }

    return obj
}

function populateView(path, name, data) {

    return new Promise( (resolve, reject) => {
        var obj = getOrCreate(callbacks.getView(), path)
        let vue = callbacks.getApp()
        if(vue && path !== '/') {
            Vue.set(obj, name, data)
        } else {
            obj[name] = data
        }
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
                .then( (data) => populateView('/admin', 'tools', data.children) )
                .then(() => resolve() )
                .catch( (error) => {
                    logger.error('call populateTools() failed')
                    reject(error)
                })
        })
    }

    populateToolsConfig() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/list.json/tools/config')
                .then( (data) => populateView('/admin', 'toolsConfig', data.children) )
                .then(() => resolve() )
        })
    }

    populateUser() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/access.json?'+(new Date()).getTime())
                .then( (data) => {
                    populateView('/state', 'user', data.userID)
                    if(data.userID === 'anonymous') {
                        alert('please login to continue')
                        window.location = '/'
                        reject()
                    }
                })
                .then(() => resolve() )
        })
    }

    populateContent(path) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/content.json'+path)
                .then( (data) => populateView('/', 'adminPageStaged', data) )
                .then(() => resolve() )
        })
    }

    populateComponents() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/components.json')
                .then( (data) => populateView('/admin', 'components', data) )
                .then( () => resolve() )
        })
    }

    populateObjects() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/objects.json')
                .then( (data) => populateView('/admin', 'objects', data) )
                .then( () => resolve() )
        })
    }

    populateTemplates() {
        return new Promise( (resolve, reject) => {
            fetch('/admin/templates.json')
                .then( (data) => populateView('/admin', 'templates', data) )
                .then( () => resolve() )
        })
    }

    populateNodesForBrowser(path, target = 'nodes', includeParents = false) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/nodes.json'+path+'?includeParents='+includeParents)
                .then( (data) => populateView('/admin', target, data ) )
                .then( () => resolve() )
                .catch( (err) => reject(err) )
        })
    }

    populateComponentDefinitionFor(component) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/components/'+component)
                .then( (data) => populateView('/admin/componentDefinitions', component, data) )
                .then( () => resolve() )
        })
    }

    populateComponentDefinitionFromNode(path) {
        return new Promise( (resolve, reject) => {
            var name;
            fetch('/admin/componentDefinition.json'+path)
                .then( (data) => {
                    name = data.name
                    let component = callbacks.getComponentByName(name)
                    if(component && component.methods && component.methods.augmentEditorSchema) {
                        data.model = component.methods.augmentEditorSchema(data.model)
                    }

                    let promises = []
                    for(let i = 0; i < data.model.fields.length; i++) {
                        let from = data.model.fields[i].valuesFrom
                        if(from) {
                            data.model.fields[i].values = []
                            let promise = axios.get(from).then( (response) => {
                                for(var key in response.data) {
                                    if(response.data[key]['jcr:title']) {
                                        const nodeName = key
                                        const val = from.replace('.infinity.json', '/'+nodeName)
                                        let name = response.data[key].name
                                        if(!name) {
                                            name = response.data[key]['jcr:title']
                                        }
                                        data.model.fields[i].values.push({ value: val, name: name })
                                    }
                                }
                            }).catch( (error) => {
                                logger.error('missing node', data.model.fields[i].valuesFrom, 'for list population in dialog', error)
                            })
                            promises.push(promise)
                        }
                        let visible = data.model.fields[i].visible
                        if(visible) {
                            data.model.fields[i].visible = function(model) { 
                                return exprEval.Parser.evaluate( visible, this );
                            } 
                        }
                    }
                    Promise.all(promises).then( () => {
                            populateView('/admin/componentDefinitions', data.name, data.model)
                            resolve(name)
                        }
                    )
                })
                .catch ( error => {
                    reject(error)
                } )
        })
    }

    populateExplorerDialog(path) {
        return this.populateComponentDefinitionFromNode(path)
    }

    populatePageView(path) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/readNode.json'+path)
                .then( (data) => populateView('/pageView', 'page', data) )
                .then( () => resolve() )
        })
    }

    populateObject(path, target, name) {
        return new Promise( (resolve, reject) => {
            // fetch('/admin/getObject.json'+path)
            //     .then( (data) => populateView(target, name, data).then( () => {
            //         this.populateComponentDefinitionFromNode(path).then( () => {
            //             resolve()
            //         })
            //     } ) )
            //
            this.populateComponentDefinitionFromNode(path).then( () => {
                fetch('/admin/getObject.json'+path).then( (data) => {
                    populateView(target, name, data).then( () => {
                        resolve();
                    })
                })
            })
        })
    }

    populateReferencedBy(path) {
        return new Promise( (resolve, reject) => {
            fetch('/admin/refBy.json'+path)
                .then( (data) => populateView('/state', 'referencedBy', data) )
                .then( () => resolve() )
        })
    }

    populateI18N(language) {
        return new Promise( (resolve, reject) => {
            axios.get('/i18n/admin/'+language+'.infinity.json').then( (response) => {
                populateView('/admin/i18n', language, response.data).then( () => {
                    resolve()
                })
            })
        })
    }

    createSite(fromName, toName) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('fromSite', fromName)
            data.append('toSite', toName)
            updateWithForm('/admin/createSite.json', data)
                .then( (data) => this.populateNodesForBrowser(callbacks.getView().state.tools.pages) )
                .then( () => resolve() )
        })
    }

    createPage(parentPath, name, templatePath) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('name', name)
            data.append('templatePath', templatePath)
            updateWithForm('/admin/createPage.json'+parentPath, data)
                .then( (data) => this.populateNodesForBrowser(parentPath) )
                .then( () => resolve() )
        })
    }

    createObject(parentPath, name, templatePath) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('name', name)
            data.append('templatePath', templatePath)
            updateWithForm('/admin/createObject.json'+parentPath, data)
                .then( (data) => this.populateNodesForBrowser(parentPath) )
                .then( () => resolve() )
        })
    }

    deleteObject(path) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            updateWithForm('/admin/deleteNode.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    deleteAsset(path) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            updateWithForm('/admin/deleteNode.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    renameAsset(path, newName) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('to', newName)
            updateWithForm('/admin/rename.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    moveAsset(path, to, type) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('to', to)
            data.append('type', type)
            updateWithForm('/admin/move.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    moveObject(path, to, type) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('to', to)
            data.append('type', type)
            updateWithForm('/admin/move.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    renameObject(path, newName) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('to', newName)
            updateWithForm('/admin/rename.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    deletePage(path) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            updateWithForm('/admin/deletePage.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    renamePage(path, newName) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('to', newName)
            updateWithForm('/admin/rename.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    movePage(path, to, type) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('to', to)
            data.append('type', type)
            updateWithForm('/admin/move.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    deletePageNode(path, nodePath) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            updateWithForm('/admin/deleteNode.json'+nodePath, data)
                .then( (data) => this.populatePageView(path) )
                .then( () => resolve() )
        })
    }

    createTemplate(parentPath, name, component) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('name', name)
            data.append('component', component)
            updateWithForm('/admin/createTemplate.json'+parentPath, data)
                .then( (data) => this.populateNodesForBrowser(parentPath) )
                .then( () => resolve() )
        })
    }

    createFolder(parentPath, name) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            data.append('name', name)
            updateWithForm('/admin/createFolder.json'+parentPath, data)
                .then( (data) => this.populateNodesForBrowser(parentPath) )
                .then( () => resolve() )
        })
    }

    deleteFolder(path) {
        return new Promise( (resolve, reject) => {
            let data = new FormData()
            updateWithForm('/admin/deleteNode.json'+path, data)
                .then( (data) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    uploadFiles(path, files, cb) {
        var config = {
          onUploadProgress: progressEvent => {
            var percentCompleted = Math.floor((progressEvent.loaded * 100) / progressEvent.total);
            cb(percentCompleted)
          }
        }

        return new Promise( (resolve, reject) => {

            logger.fine('uploading files to', path)
            logger.fine(files)

            var data = new FormData()
            for(var i = 0; i < files.length; i++) {
                var file = files[i]
                data.append(file.name, file, file.name)
            }

            updateWithFormAndConfig('/admin/uploadFiles.json'+path, data, config)
                .then( (response) => this.populateNodesForBrowser(path) )
                .then( () => resolve() )
        })
    }

    fetchExternalImage(path, url, name, config) {
        return new Promise( (resolve, reject) => {
            axios.get(url, {responseType: "blob"}).then( (response) => {
                //alert(response.data)
                var data = new FormData()
                data.append(name, response.data, name)

                updateWithFormAndConfig('/admin/uploadFiles.json'+path, data, config)
                    .then( (response) => this.populateNodesForBrowser(path) )
                    .then( () => resolve() )
                    .catch( err => reject() )
            })
        })
    }

    setInitialPageEditorState() {
        return new Promise( (resolve, reject) => {
            populateView('/state', 'editorVisible', false)
            populateView('/state', 'rightPanelVisible', true)
            resolve()
        })
    }

    savePageEdit(path, node) {
        return new Promise( (resolve, reject) => {
            let formData = new FormData()
            // convert to a new object
            let nodeData = JSON.parse(JSON.stringify(node))
            if(nodeData.component) {
                let component = callbacks.getComponentByName(nodeData.component)
                if(component && component.methods && component.methods.beforeSave) {
                    nodeData = component.methods.beforeSave(nodeData)
                }
            }
            delete nodeData['children']
            delete nodeData['path']
            delete nodeData['component']
            nodeData['jcr:primaryType'] = 'nt:unstructured'
            if(node.component) {
                nodeData['sling:resourceType'] = node.component.split('-').join('/')
            }
            stripNulls(nodeData)
            formData.append('content', JSON.stringify(nodeData))

            updateWithForm('/admin/updateResource.json'+path + node.path, formData)
                // .then( (data) => this.populateNodesForBrowser(parentPath) )
                .then( () => resolve() )
        })
    }

    saveObjectEdit(path, node) {
        return new Promise( (resolve, reject) => {
            let formData = new FormData()
            // convert to a new object
            let nodeData = JSON.parse(JSON.stringify(node))
            stripNulls(nodeData)
            delete nodeData['jcr:created']
            delete nodeData['jcr:createdBy']
            delete nodeData['jcr:lastModified']
            delete nodeData['jcr:lastModifiedBy']
            formData.append('content', JSON.stringify(nodeData))

            updateWithForm('/admin/updateResource.json'+path, formData)
                .then( () => resolve() )
        })
    }

    insertNodeAt(path, component, drop, variation) {
        logger.fine(arguments)
        return new Promise( (resolve, reject) => {
            let formData = new FormData();
            formData.append('component', component)
            formData.append('drop', drop)
            if(variation) {
                formData.append('variation', variation)
            }
            updateWithForm('/admin/insertNodeAt.json'+path, formData)
                .then( function(data) {
                    resolve(data)
                })
        })
    }

    insertNodeWithDataAt(path, data, drop) {
        logger.fine(arguments)
        return new Promise( (resolve, reject) => {
            let formData = new FormData();
            formData.append('content', JSON.stringify(data))
            formData.append('drop', drop);
            updateWithForm('/admin/insertNodeAt.json'+path, formData)
                .then( (data) => {
                    resolve(data)
                })
        })
    }

    moveNodeTo(path, component, drop) {
        logger.fine('Move Node To: path: ' + path + ', component: ' + component + ', drop: ' + drop)
        return new Promise( (resolve, reject) => {
            let formData = new FormData();
            formData.append('component', component)
            formData.append('drop', drop)
            updateWithForm('/admin/moveNodeTo.json'+path, formData)
                .then( function(data) {
                    resolve(data)
                })
        })
    }

    replicate(path) {
        return new Promise( (resolve, reject) => {
            let formData = new FormData();
            formData.append('deep', 'false')
            formData.append('name', 'defaultRepl')
            updateWithForm('/admin/repl.json'+path, formData)
                .then( function(data) {
                    resolve(data)
                })
        })
    }
}

export default PerAdminImpl
