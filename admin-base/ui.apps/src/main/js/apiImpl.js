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

import {LoggerFactory} from './logger'
import {stripNulls} from './utils'
import {Field} from './constants';

let logger = LoggerFactory.logger('apiImpl').setLevelDebug()

const API_BASE = '/perapi'
const postConfig = {
  withCredentials: true
}

let callbacks

function json(data) {
  const content = JSON.stringify(data)
  return new Blob([content], {type: 'application/json; charset=utf-8'})
}

function fetch(path) {
  logger.fine('Fetch ', path)
  return axios.get(API_BASE + path).then((response) => {
    return new Promise((resolve, reject) => {

      // Fix for IE11
      if ((typeof response.data === 'string' && response.data.startsWith(
          '<!DOCTYPE')) || (response.request && response.request.responseURL
          && response.request.responseURL.indexOf('/system/sling/form/login')
          >= 0)) {
        window.location = '/system/sling/form/login'
        reject('need to authenticate')
      }
      resolve(response.data)
    })
  }).catch((error) => {
    logger.error('Fetch request to', path, 'failed')
    throw error
  })

}

function update(path) {
  logger.fine('Update, path: ', path)
  return axios.post(API_BASE + path, null, postConfig)
      .then((response) => {
        logger.fine('Update, response data: ' + response.data)
        return response.data
      })
      .catch((error) => {
        logger.error('Update request to', path, 'failed')
        throw error
      })
}

function updateWithForm(path, data) {
  logger.fine('Update with Form, path: ' + path + ', data: ' + data)
  return axios.post(API_BASE + path, data, postConfig)
      .then((response) => response.data)
      .catch((error) => {
        logger.error('Update with Form request to', path, 'failed')
        throw error
      })
}

function updateWithFormAndConfig(path, data, config) {
  //AS TODO: How to merge config into postConfig or the other way around?
  // config.withCredentials: true
  logger.fine('Update with Form and Config, path: ' + path + ', data: ' + data)
  return axios.post(API_BASE + path, data, config)
      .then((response) => {
        logger.fine(
            'Update with Form and Config, response data: ' + response.data)
        return response.data
      })
      .catch((error) => {
        logger.error('Update with Form and Config request to',
            error.response.request.path, 'failed')
        throw error
      })
}

function getOrCreate(obj, path) {

  if (path === '/') {
    // do nothing, requesting root
  } else {
    var segments = path.split('/').slice(1).reverse()

    while (segments.length > 0) {
      var segment = segments.pop()
      if (!obj[segment]) {
        Vue.set(obj, segment, {})
//                obj[segment] = {}
      }
      obj = obj[segment]
    }
  }

  return obj
}

function populateView(path, name, data) {

  return new Promise((resolve) => {
    var obj = getOrCreate(callbacks.getView(), path)
    let vue = callbacks.getApp()
    if (vue && path !== '/') {
      Vue.set(obj, name, data)
    } else {
      obj[name] = data
    }
    resolve()
  })

}

// function updateExplorerDialog() {
//   const view = callbacks.getView()
//   const page = get(view, '/state/tools/page', '')
//   const template = get(view, '/state/tools/template', '')
//   if (page) {
//     $perAdminApp.stateAction('showPageInfo', {selected: page})
//   }
//   if (template) {
//     $perAdminApp.stateAction('showPageInfo', {selected: template})
//   }
// }

function translateFields(fields) {
  const $i18n = Vue.prototype.$i18n
  if(!$i18n) return fields
  if (!fields || fields.length <= 0) {
    return
  }
  for (let i = 0; i < fields.length; i++) {
    const field = fields[i]
    if (field) {
      if (field.label) {
        const label = field.label.split(':').join('..')
        fields[i].label = $i18n(label)
      }
      if (field.placeholder) {
        const placeholder = field.placeholder.split(':').join('..')
        fields[i].placeholder = $i18n(placeholder)
      }
      if (field.hint) {
        let split = field.hint.split('. ')
        if (split.length <= 1) {
          fields[i].hint = $i18n(field.hint)
        } else {
          for (let j = 0; j < split.length; j++) {
            let item = split[j]
            if (item.length > 0) {
              split[j] = $i18n(item)
            }
          }
          fields[i].hint = split.join('. ')
        }
      }
      if (field.type === Field.SWITCH) {
        fields[i].textOn = $i18n(field.textOn)
        fields[i].textOff = $i18n(field.textOff)
      } else if (field.type === Field.SELECT) {
        const values = fields[i].values;
        for (let j = 0; j < values.length; j++) {
          const name = values[j].name
          const t = $i18n(name)
          fields[i].values[j].name = t.startsWith('T[') ? name : t
        }
      } else if (field.type === Field.MULTI_SELECT) {
        if (field.selectOptions.placeholder) {
          const placeholder = field.selectOptions.placeholder
          field.selectOptions.placeholder = $i18n(placeholder)
        }
      }
    }
  }
}

class PerAdminImpl {

  constructor(cb) {
    callbacks = cb
  }

  populateTools() {
    return fetch('/admin/list.json/tools')
        .then((data) => populateView('/admin', 'tools', data.children))
        .catch((error) => {
          logger.error('call populateTools() failed')
          return error
        })
  }

  populateToolsConfig() {
    return fetch('/admin/list.json/tools/config')
        .then((data) => populateView('/admin', 'toolsConfig', data.children))
  }

  populateUser() {
    return fetch('/admin/access.json?' + (new Date()).getTime())
        .then((data) => {
          return populateView('/state', 'user', data.userID).then(() => {
            if (data.userID === 'anonymous') {
              // alert('please login to continue')
              window.location = '/'
            }
            return populateView('/state', 'userPreferences', data.preferences)
          })
        })
  }

  populateContent(path) {
    return fetch('/admin/content.json' + path)
        .then((data) => populateView('/', 'adminPageStaged', data))
  }

  populateComponents() {
    return fetch('/admin/components.json')
        .then((data) => populateView('/admin', 'components', data))
  }

  populateObjects() {
    return fetch('/admin/objects.json')
        .then((data) => populateView('/admin', 'objects', data))
  }

  populateTemplates() {
    return fetch('/admin/templates.json')
        .then((data) => populateView('/admin', 'templates', data))
  }

  populateSkeletonPages(path, target = 'skeletonNodes',
      includeParents = false) {
    const skeletonPagePath = path.split('/').slice(0, 4).join('/')
        + '/skeleton-pages'

    // try {
    //   if (get(skeletonPagePath, null)) {
    //     this.populateContent(skeletonPagePath)
    //   }
    // } catch(err) {}

    return this.populateNodesForBrowser(skeletonPagePath, target,
        includeParents)
  }

  populateNodesForBrowser(path, target = 'nodes', includeParents = false) {
    return fetch(
        '/admin/nodes.json' + path + '?includeParents=' + includeParents)
        .then((data) => populateView('/admin', target, data))
  }

  populateComponentDefinitionFor(component) {
    return fetch('/admin/components/' + component)
        .then((data) => populateView('/admin/componentDefinitions', component,
            data))
  }

  populateComponentDefinitionFromNode(path) {
    return new Promise((resolve, reject) => {
      var name;
      fetch('/admin/componentDefinition.json' + path)
          .then((data) => {
            name = data.name
            let component = callbacks.getComponentByName(name)
            if (component && component.methods
                && component.methods.augmentEditorSchema) {
              data.model = component.methods.augmentEditorSchema(data.model)
              data.ogTags = component.methods.augmentEditorSchema(data.ogTags)
            }

            let promises = []
            if (data && data.model) {
              if (data.model.groups) {
                for (let j = 0; j < data.model.groups.length; j++) {
                  for (let i = 0; i < data.model.groups[j].fields.length; i++) {
                    let from = data.model.groups[j].fields[i].valuesFrom
                    if (from) {
                      data.model.groups[j].fields[i].values = []
                      let promise = axios.get(from).then((response) => {
                        for (var key in response.data) {
                          if (response.data[key]['jcr:title']) {
                            const nodeName = key
                            const val = from.replace('.infinity.json',
                                '/' + nodeName)
                            let name = response.data[key].name
                            if (!name) {
                              name = response.data[key]['jcr:title']
                            }
                            data.model.groups[j].fields[i].values.push(
                                {value: val, name: name})
                          }
                        }
                      }).catch((error) => {
                        logger.error('missing node',
                            data.model.groups[j].fields[i].valuesFrom,
                            'for list population in dialog', error)
                      })
                      promises.push(promise)
                    }
                    let visible = data.model.groups[j].fields[i].visible
                    if (visible) {
                      data.model.groups[j].fields[i].visible = function (model) {
                        return exprEval.Parser.evaluate(visible, this);
                      }
                    }
                  }
                }
              } else {
                for (let i = 0; i < data.model.fields.length; i++) {
                  let from = data.model.fields[i].valuesFrom
                  if (from) {
                    data.model.fields[i].values = []
                    let promise = axios.get(from).then((response) => {
                      const toProcess = []
                      for (let key in response.data) {
                        toProcess.push({key, data: response.data[key]})
                      }

                      let next = toProcess.shift()
                      while (next) {
                        if (next.data['jcr:title']) {
                          const nodeName = next.key
                          const val = next.data.path ? next.data.path + '/'
                              + nodeName
                              : from.replace('.infinity.json', '/' + nodeName)
                          let name = next.data.name
                          if (!name) {
                            name = next.data['jcr:title']
                          }
                          if (next.parent) {
                            name = next.parent + '-' + name;
                          }
                          data.model.fields[i].values.push(
                              {value: val, name: name})
                          for (let k in next.data) {
                            if (next.data[k] instanceof Object
                                && next.data[k]['sling:resourceType']
                                === 'admin/objects/tag') {
                              toProcess.push(
                                  {key: k, parent: name, data: next.data[k]})
                            }
                          }
                        }
                        next = toProcess.shift()
                      }

                    }).catch((error) => {
                      logger.error('missing node',
                          data.model.fields[i].valuesFrom,
                          'for list population in dialog', error)
                    })
                    promises.push(promise)
                  }
                  const visible = data.model.fields[i].visible
                  if (visible) {
                    data.model.fields[i].visible = function (model) {
                      return exprEval.Parser.evaluate(visible, this);
                    }
                  }
                }
                if (data.ogTags) {
                  for (let i = 0; i < data.ogTags.fields.length; i++) {
                    let from = data.ogTags.fields[i].valuesFrom
                    if (from) {
                      data.ogTags.fields[i].values = []
                      let promise = axios.get(from).then((response) => {
                        for (var key in response.data) {
                          if (response.data[key]['jcr:title']) {
                            const nodeName = key
                            const val = from.replace('.infinity.json',
                                '/' + nodeName)
                            let name = response.data[key].name
                            if (!name) {
                              name = response.data[key]['jcr:title']
                            }
                            data.ogTags.fields[i].values.push(
                                {value: val, name: name})
                          }
                        }
                      }).catch((error) => {
                        logger.error('missing node',
                            data.ogTags.fields[i].valuesFrom,
                            'for list population in dialog', error)
                      })
                      promises.push(promise)
                    }
                    const visible = data.ogTags.fields[i].visible
                    if (visible) {
                      data.ogTags.fields[i].visible = function (ogTags) {
                        return exprEval.Parser.evaluate(visible, this);
                      }
                    }
                  }
                  translateFields(data.ogTags.fields)
                }
                translateFields(data.model.fields)
              }
            } else {
              logger.warn(`no dialog.json file given for component "${name}"`)
            }

            Promise.all(promises).then(() => {
              populateView('/admin/componentDefinitions', data.name, data)
              resolve(name)
            })
          })
    }).catch(error => {
      reject(error)
    })
  }

  populateExplorerDialog(path) {
    return this.populateComponentDefinitionFromNode(path)
  }

  populateTenants() {
    return new Promise((resolve, reject) => {
      fetch('/admin/listTenants.json')
          .then((data) => {
            // const state = callbacks.getView().state
            // if (!state.tenant && data.tenants.length > 0) {
            //   $perAdminApp.stateAction('setTenant',
            //       data.tenants[data.tenants.length - 1])
            //       .then(() => populateView('/admin', 'tenants', data.tenants))
            //       .then(() => resolve())
            // } else {
              populateView('/admin', 'tenants', data.tenants)
                  .then(() => resolve())
            // }
          })
    })
  }

  populateBackupInfo(backup) {
    let tenantName = backup ? backup.tenant : '';
    if(tenantName === '' || tenantName === 'undefined') {
      const tenant = $perAdminApp.getNodeFromViewWithDefault("/state/tenant", {});
      tenantName = tenant ? tenant.name : '';
    }
    fetch('/admin/backupTenant.json/content/' + tenantName)
        .then((data) => populateView('/state/tools', 'backup', data));
  }

  populatePageView(path) {
    return fetch('/admin/readNode.json' + path)
        .then((data) => populateView('/pageView', 'page', data))
  }

  populateObject(path, target, name) {
    return this.populateComponentDefinitionFromNode(path)
        .then(() => {
          return fetch('/admin/getObject.json' + path)
              .then((data) => populateView(target, name, data))
        })
  }

  populateReferencedBy(path) {
    return fetch('/admin/refBy.json' + path)
        .then((data) => populateView('/state', 'referencedBy', data))
  }

  populateReferences(path) {
    return new Promise((resolve, reject) => {
      fetch(`/admin/ref.json${path}`)
        .then(function(result) {
            populateView('/state', 'references', result)
                .then(() => resolve())
        })
        .catch(error => {
            if (error.response && error.response.data && error.response.data.message) {
                reject(error.response.data.message)
            }
        })
    })
  }

  populateI18N(language) {
    return new Promise((resolve, reject) => {
      axios.get('/i18n/admin/' + language + '.infinity.json')
          .then((response) => {
            populateView('/admin/i18n', language, response.data)
                .then(() => resolve())
          })
    })
  }


  populateRecyclebin(page = 0) {
    let tenant = getOrCreate(callbacks.getView(), '/state/tenant').name
    if (tenant == undefined) {
        tenant = callbacks.getView().state.tenant.name
    }
    if (page instanceof Object) {
        page = 0;
    }
    return new Promise((resolve, reject) => {
        fetch(`/admin/listRecyclables.json/content/${tenant}?page=${page}`)
            .then(function(result) {
                 populateView('/admin', 'recyclebin', result)
                    .then(() => resolve())
            })
            .catch(error => {
                $perAdminApp.notifyUser('error', `${error}. Unable to load Recycle Bin`)
            })
    })
  }

  populateVersions(page) {
    if (page) {
        return new Promise((resolve, reject) => {
          fetch(`/admin/listVersions.json${page}`)
            .then(function(result) {
                populateView('/state', 'versions', result)
                    .then(() => resolve())
            })
            .catch(error => {
                if (error.response && error.response.data && error.response.data.message) {
                    reject(error.response.data.message)
                }
            })
        })
    }
  }

  recycleItem(item) {
      return new Promise((resolve, reject) => {
        let data = new FormData()
        updateWithForm('/admin/restoreRecyclable.json' + item.recyclebinItemPath, data)
            .then( (data) => callbacks.getApi().populateRecyclebin(0) )
            .then(() => resolve())
            .catch(error => {
                if (error.response && error.response.data && error.response.data.message) {
                    reject(error.response.data.message)
                }
                reject(error)
            })
      })
  }

  deleteRecyclable(path) {
        return new Promise((resolve, reject) => {
            let data = new FormData()
            updateWithForm('/admin/deleteNode.json' + path, data)
                .then( (data) => callbacks.getApi().populateRecyclebin(0))
                .then(() => resolve())
                .catch(error => {
                    if (error.response && error.response.data && error.response.data.message) {
                        reject(error.response.data.message)
                    }
                    reject(error)
                })
        })
  }

  deleteVersion(info) {
        return new Promise((resolve, reject) => {
            let data = new FormData()
            data.append('action', 'deleteVersion')
            data.append('version', info.version)
            updateWithForm('/admin/manageVersions.json' + info.path, data)
                .then( (data) => callbacks.getApi().populateVersions(info.path))
                .then(() => resolve())
                .catch(error => {
                    if (error.response && error.response.data && error.response.data.message) {
                        reject(error.response.data.message)
                    }
                    reject(error)
                })
        })
  }

  createVersion(path) {
      return new Promise((resolve, reject) => {
          let data = new FormData()
          data.append('action', 'createVersion')
          updateWithForm('/admin/manageVersions.json' + path, data)
              .then( (data) => callbacks.getApi().populateVersions(path))
              .then(() => resolve())
              .catch(error => {
                  if (error.response && error.response.data && error.response.data.message) {
                      reject(error.response.data.message)
                  }
                  reject(error)
              })
      })
  }

  restoreVersion(path, versionName) {
        return new Promise((resolve, reject) => {
            let data = new FormData()
            data.append('action', 'restoreVersion')
            data.append('version', versionName)
            updateWithForm('/admin/manageVersions.json' + path, data)
                .then( (data) => callbacks.getApi().populateVersions(path))
                .then( function(){
                    if (path.includes("/assets/")) {
                        $perAdminApp.loadContent("/content/admin/pages/assets")
                    } else {
                         callbacks.getApi().populatePageView(path)
                            .then(function(){
                                const editView = document.getElementById('editview')
                                if (editView) {
                                    editView.contentWindow.$peregrineApp.loadContent(path+ '.html')
                                }
                            })
                            .then(function(){
                                let nodes = ''
                                const pagesRgx = new RegExp('^\/content\/[^\/]+\/pages\/');
                                const templatesRgx = new RegExp('^\/content\/[^\/]+\/templates\/');
                                if (pagesRgx.test(path)) {
                                    nodes = $perAdminApp.getView().state.tools.pages
                                } else if (templatesRgx.test(path)){
                                    nodes = $perAdminApp.getView().state.tools.templates
                                }
                                if (nodes != '') {
                                    $perAdminApp.getApi().populateNodesForBrowser(nodes)
                                }
                            })
                    }
                })
                .then( () => resolve())
                .catch(error => {
                    if (error.response && error.response.data && error.response.data.message) {
                        reject(error.response.data.message)
                    } else {
                        reject(error)
                    }
                })
        })
  }

  createTenant(fromName, toName, tenantTitle, tenantUserPwd, colorPalette) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('fromTenant', fromName)
      data.append('toTenant', toName)
      data.append('tenantTitle', tenantTitle)
      data.append('tenantUserPwd', tenantUserPwd)
      if (colorPalette) {
        data.append('colorPalette', colorPalette)
      }
      updateWithForm('/admin/createTenant.json', data)
          .then((data) => this.populateNodesForBrowser(
              callbacks.getView().state.tools.pages))
          .then(() => resolve())
          .catch(error => {
              if (error.response && error.response.data && error.response.data.message) {
                  reject(error.response.data.message)
              }
              reject(error)
            })
    })
  }

  createPage(parentPath, name, templatePath, title) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('name', name)
      data.append('templatePath', templatePath)
      data.append('title', title)
      updateWithForm('/admin/createPage.json' + parentPath, data)
          .then((data) => {
            if (parentPath.indexOf('skeleton-pages') > -1) {
              this.populateSkeletonPages(parentPath)
            }
            this.populateNodesForBrowser(parentPath)
          })
          .then(() => resolve())
    })
  }

  createPageFromSkeletonPage(parentPath, name, skeletonPagePath) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('path', skeletonPagePath)
      data.append('to', parentPath)
      data.append('deep', 'true')
      data.append('newName', name)
      data.append('newTitle', name)
      data.append('type', 'child')
      updateWithForm('/admin/createPageFromSkeletonPage.json', data)
          .then((data) => this.populateNodesForBrowser(parentPath))
          .then(() => resolve())
    })
  }

  createObject(parentPath, name, templatePath) {
    let data = new FormData()
    data.append('name', name)
    data.append('templatePath', templatePath)
    return updateWithForm('/admin/createObject.json' + parentPath, data)
        .then(() => this.populateNodesForBrowser(parentPath))
  }

  deleteObject(path) {
    let data = new FormData()
    return updateWithForm('/admin/deleteNode.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  deleteAsset(path) {
    let data = new FormData()
    return updateWithForm('/admin/deleteNode.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  renameAsset(path, newName, newTitle) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('to', newName)
      data.append('title', newTitle)
      updateWithForm('/admin/asset/rename.json' + path, data)
          .then((data) => this.populateNodesForBrowser(path))
          .then(() => resolve())
          .catch(error => {
              logger.error('Failed to change name: ' + error)
              reject('Unable to change name. ' + error)
          })
    })
  }

  moveAsset(path, to, type) {
    let data = new FormData()
    data.append('to', to)
    data.append('type', type)
    return updateWithForm('/admin/move.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  moveObject(path, to, type) {
    let data = new FormData()
    data.append('to', to)
    data.append('type', type)
    return updateWithForm('/admin/move.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  renameObject(path, newName) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('to', newName)
      updateWithForm('/admin/object/rename.json' + path, data)
          .then((data) => this.populateNodesForBrowser(path))
          .then(() => resolve())
    })
  }

  deletePage(path) {
    const data = new FormData()
    return updateWithForm('/admin/deletePage.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  deleteTenant(target) {
    const name = target.name;
    const root = '/content'
    const data = new FormData()
    data.append('name', name)
    return updateWithForm('/admin/deleteTenant.json', data)
        .then(() => this.populateNodesForBrowser(root))
  }

  renamePage(path, newName, newTitle) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('to', newName)
      data.append('title', newTitle)
      updateWithForm('/admin/page/rename.json' + path, data)
          .then((data) => this.populateNodesForBrowser(path))
          .then(() => resolve())
          .catch(error => {
              logger.error('Failed to change name: ' + error)
              reject('Unable to change name. ' + error)
          })
    })
  }

  copyPage(srcPath, targetPath) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('path', srcPath)
      data.append('to', targetPath)
      data.append('deep', 'true')
      data.append('newName', name)
      data.append('newTitle', name)
      data.append('type', 'child')
      updateWithForm('/admin/createPageFromSkeletonPage.json', data)
          .then((data) => this.populateNodesForBrowser(srcPath))
          .then(() => resolve())
    })
  }

  movePage(path, to, type) {
    let data = new FormData()
    data.append('to', to)
    data.append('type', type)
    return updateWithForm('/admin/move.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  deletePageNode(path, nodePath) {
    let data = new FormData()
    return updateWithForm('/admin/deleteNode.json' + nodePath, data)
        .then(() => this.populatePageView(path))
  }

  createTemplate(parentPath, name, component, title) {
    let data = new FormData()
    data.append('name', name)
    data.append('component', component)
    data.append('title', title)
    return updateWithForm('/admin/createTemplate.json' + parentPath, data)
        .then(() => this.populateNodesForBrowser(parentPath))
  }

  createObjectDefinition(parentPath, name) {
    let data = new FormData()
    data.append('name', name)
    return updateWithForm('/admin/createObjectDefinition.json' + parentPath, data)
    .then(() => this.populateNodesForBrowser(parentPath))
  }

  moveTemplate(path, to, type) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      data.append('to', to)
      data.append('type', type)
      updateWithForm('/admin/move.json' + path, data)
          .then((data) => this.populateNodesForBrowser(path))
          .then(() => resolve())
    })
  }

  deleteTemplate(path) {
    return new Promise((resolve, reject) => {
      let data = new FormData()
      updateWithForm('/admin/deleteNode.json' + path, data)
          .then((data) => this.populateNodesForBrowser(path))
          .then(() => resolve())
    })
  }

  createFolder(parentPath, name) {
    let data = new FormData()
    data.append('name', name)
    return updateWithForm('/admin/createFolder.json' + parentPath, data)
        .then(() => this.populateNodesForBrowser(parentPath))
  }

  deleteFolder(path) {
    let data = new FormData()
    return updateWithForm('/admin/deleteNode.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  deleteFile(path) {
    let data = new FormData()
    return updateWithForm('/admin/deleteNode.json' + path, data)
        .then(() => this.populateNodesForBrowser(path))
  }

  uploadFiles(path, files, cb) {
    const config = {
      onUploadProgress: progressEvent => {
        const percentCompleted = Math.floor(
            (progressEvent.loaded * 100) / progressEvent.total);
        cb(percentCompleted)
      }
    }
    const data = new FormData()
    const fileNamesNotUploaded = []
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      const available = this.nameAvailable(file.name, path)
      if (available) {
        data.append(file.name, file, file.name)
      } else {
        if (files.length == 1) {
          // if user is uploading 1 assets && name not available,
          // Then ask user whether to 'keep both' or 'replace'
          $perAdminApp.askUser('File exists',
              'Select to replace the existing one, or keep both', {
                yesText: 'Replace',
                noText: 'Keep both',
                yes() {
                  const $api = $perAdminApp.getApi()
                  logger.info(
                      'user selected \'replace\' upload file ' + file.name)
                  const replaceData = new FormData()
                  replaceData.append(file.name, file, file.name)
                  return updateWithFormAndConfig(
                      '/admin/uploadFiles.json' + path, replaceData, config)
                      .then(() => $api.populateNodesForBrowser(path))
                      .catch(error => {
                        log.error('Failed to upload: ' + error)
                        reject('Unable to upload due to an error. ' + error)
                      })
                },
                no() {
                  logger.info(
                      'user selected \'keep both\' make the uploaded file name unique and upload')
                  const $api = $perAdminApp.getApi()
                  let localNamePart = file.name
                  let extensionPart = ''
                  const indexOfLasDot = file.name.lastIndexOf('.');
                  let newFileName
                  if (indexOfLasDot > 0) {
                    // filename has a dot
                    localNamePart = file.name.substring(0, indexOfLasDot);
                    extensionPart = file.name.substring(indexOfLasDot,
                        file.name.length);
                  }
                  let i = 1
                  do {
                    newFileName = localNamePart + i++ + extensionPart
                  } while (!$api.nameAvailable(newFileName, path));
                  const keepbothData = new FormData()
                  keepbothData.append(newFileName, file, newFileName)
                  return updateWithFormAndConfig(
                      '/admin/uploadFiles.json' + path, keepbothData, config)
                      .then(() => $api.populateNodesForBrowser(path))
                      .catch(error => {
                        log.error('Failed to upload: ' + error)
                        reject('Unable to upload due to an error. ' + error)
                      })
                }
              })
        } else {
          fileNamesNotUploaded.push(file.name)
        }
      }
    }
    if (fileNamesNotUploaded.length > 0) {
      $perAdminApp.notifyUser('Info',
          'Some assets were not uploaded. Asset exists in this location: ' +
          fileNamesNotUploaded.toString())
    }
//    if there are eny entries
    if (!data.entries().next().done) {
      return updateWithFormAndConfig('/admin/uploadFiles.json' + path, data,
          config)
          .then(() => this.populateNodesForBrowser(path))
          .catch(error => {
            log.error('Failed to upload: ' + error)
            reject('Unable to upload due to an error. ' + error)
          })
    }
    return
  }

  nameAvailable(value, path) {
    if (!value || value.length === 0) {
      return false
    } else {
      const folder = $perAdminApp.findNodeFromPath(
          $perAdminApp.getView().admin.nodes, path)
      for (let i = 0; i < folder.children.length; i++) {
        if (folder.children[i].name === value) {
          return false
        }
      }
    }
    return true
  }

  fetchExternalImage(path, url, name, config) {
    return axios.get(url, {responseType: 'blob'})
        .then((response) => {
          var data = new FormData()
          data.append(name, response.data, name)

          return updateWithFormAndConfig('/admin/uploadFiles.json' + path, data,
              config)
              .then(() => this.populateNodesForBrowser(path))
        })
  }

  setInitialPageEditorState(path) {
    return new Promise((resolve, reject) => {
      populateView('/state', 'editorVisible', false)
      populateView('/state', 'rightPanelVisible', true)
      populateView('/state', 'editor', {})

      try {
        const page = path
        const pagePath = page.split('/')
        const type = pagePath[3]
        pagePath.pop()
        if(type === 'pages') {
          callbacks.getView().state.tools.pages = pagePath.join('/')
        } else if(type === 'templates') {
          callbacks.getView().state.tools.templates = pagePath.join('/')
        }
        return $perAdminApp.stateAction('showPageInfo', { selected: page }).then( () => {
          resolve()
        })
      } catch(error) {
        logger.error('setting of path in initial page editor state failed')
        logger.error(error)
        reject()
      }
    })
  }

  savePageEdit(path, node) {
    return new Promise((resolve, reject) => {
      let formData = new FormData()
      // convert to a new object
      let nodeData = JSON.parse(JSON.stringify(node))
      if (nodeData.component) {
        let component = callbacks.getComponentByName(nodeData.component)
        if (component && component.methods && component.methods.beforeSave) {
          nodeData = component.methods.beforeSave(nodeData)
        }
      }
      if (nodeData.path === '/jcr:content') {
        nodeData['jcr:primaryType'] = 'per:PageContent'
      } else {
        nodeData['jcr:primaryType'] = 'nt:unstructured'
      }

      delete nodeData['children']
      delete nodeData['path']
      delete nodeData['component']
      if (node.component) {
        nodeData['sling:resourceType'] = node.component.split('-').join('/')
      }
      stripNulls(nodeData)

      formData.append('content', json(nodeData))

      updateWithForm('/admin/updateResource.json' + path + node.path, formData)
      // .then( (data) => this.populateNodesForBrowser(parentPath) )
      .then(() => resolve())
      .catch( error => {
         logger.error('Failed to save page: ' + error)
         reject('Unable to save change. '+ error)
       })
    })
  }

  saveObjectEdit(path, node) {
    let formData = new FormData()
    // convert to a new object
    let nodeData = JSON.parse(JSON.stringify(node))
    stripNulls(nodeData)
    delete nodeData['jcr:created']
    delete nodeData['jcr:createdBy']
    delete nodeData['jcr:lastModified']
    delete nodeData['jcr:lastModifiedBy']
    formData.append('content', json(nodeData))
    return updateWithForm('/admin/updateResource.json' + path, formData)
  }

  saveAssetProperties(node) {
    return new Promise((resolve, reject) => {
      let formData = new FormData()
      // convert to a new object
      let nodeData = JSON.parse(JSON.stringify(node))
      stripNulls(nodeData)
      delete nodeData['name']
      delete nodeData['path']
      delete nodeData['created']
      delete nodeData['createdBy']
      delete nodeData['lastModified']
      delete nodeData['lastModifiedBy']
      formData.append('content', json(nodeData))
      updateWithForm('/admin/updateResource.json' + node.path + '/jcr:content',
          formData)
          .then(() => resolve())
    })
  }

  insertNodeAt(path, component, drop, variation) {
    logger.fine(arguments)
    let formData = new FormData();
    formData.append('component', component)
    formData.append('drop', drop)
    if (variation) {
      formData.append('variation', variation)
    }
    return new Promise((resolve) => {
      updateWithForm('/admin/insertNodeAt.json' + path, formData)
          .then(function (data) {
            resolve(data)
          }).catch(() => {
            resolve({})
          }
      )
    })
  }

  insertNodeWithDataAt(path, data, drop) {
    logger.fine(arguments)
    let formData = new FormData();
    formData.append('content', json(data))
    formData.append('drop', drop);
    return new Promise((resolve) => {
      updateWithForm('/admin/insertNodeAt.json' + path, formData)
          .then((data) => resolve(data))
          .catch(() => resolve({}))
    })
  }

  moveNodeTo(path, component, drop) {
    logger.fine(
        'Move Node To: path: ' + path + ', component: ' + component + ', drop: '
        + drop)
    let formData = new FormData();
    formData.append('component', component)
    formData.append('drop', drop)
    return updateWithForm('/admin/moveNodeTo.json' + path, formData)
  }

replicate(path, replicationService='', deep=false, deactivate=false, resources=[]) {
  const timeNow = Date.now() - 1000
  let noticeFunction = undefined
  let count = 0
  console.log(`time now = ${timeNow}`)
    return new Promise((resolve, reject) => {
      let formData = new FormData();
      formData.append('deep', deep)
      formData.append('name', replicationService)
      formData.append('deactivate', deactivate)
      resources.forEach((ref) => formData.append('resources', ref))      
      updateWithForm('/admin/repl.json' + path, formData)
          .then((respData)=>{
            count = 0
            noticeFunction = setInterval(function(){    
                return fetch(`/admin/listReplicationStatus.json${respData.sourcePath}`)
                  .then((data) => {
                    let stopPolling = false
                    if (count++ >= 25) {
                      stopPolling = true
                      clearInterval(noticeFunction)
                      $perAdminApp.notifyUser('Error', `Action timed out when ${deactivate?'un':''}publishing ${data.sourcePath}.`)
                      reject()
                      return                      
                    }
                    if (data['per:ReplicationLastAction'] === "deactivated" && data['activated'] === false && !data['per:ReplicationRef']) {
                      stopPolling = true
                    } else if (data['per:ReplicationLastAction'] === "activated" 
                        && data['activated'] === true  && data['per:Replicated'] && timeNow < Date.parse(data['per:Replicated'])
                        && data['per:ReplicationRef'] && data['per:ReplicationRef'].indexOf("pending") < 0) {
                          stopPolling = true
                    }

                    if (stopPolling) {
                        const parentPath = path.substring(0, path.lastIndexOf("/"))
                        $perAdminApp.getApi().populateNodesForBrowser(parentPath)
                        clearInterval(noticeFunction)
                        $perAdminApp.notifyUser('Success', `${data.sourcePath} was successfuly ${deactivate?'un':''}published.`)
                      }
                  })
              }, 500);
          })
          .then(() => resolve())
          .catch(error => {            
              clearInterval(noticeFunction)
              $perAdminApp.notifyUser('Errors', `were encountered when ${deactivate?'un':''}publishing ${data.sourcePath}. Please check with your admin.`)
              if (error.response && error.response.data && error.response.data.message) {
                reject(error.response.data.message)
              }
              reject(error)
          })
    })  
  }


  getPalettes(templateName) {
    return fetch(`/admin/nodes.json/content/${templateName}/pages/css/palettes`)
        .then((data) => {
          return $perAdminApp.findNodeFromPath(data,
              `/content/${templateName}/pages/css/palettes`)
        }).catch((err) => {
          logger.warn(`template ${templateName} does not support palettes`)
        })
  }

  populateIcons(tenant) {
    return fetch(`/admin/nodes.json/content/${tenant.name}/assets/icons`)
        .then((data) => {
          const iconsNode = $perAdminApp.findNodeFromPath(data,
              `/content/${tenant.name}/assets/icons`)
          const icons = iconsNode.children
          Vue.set($perAdminApp.getView().admin, 'icons', icons)
          logger.debug(`populated icons for tenant ${tenant.name}:`, icons)
        }).catch((err) => {
          logger.warn(`tenant ${tenant.name} does not have any icons`)
        })
  }

  tenantSetupReplication(path, withSite) {
    let formData = new FormData();
    formData.append('withSite', withSite)
    return updateWithForm('/admin/tenantSetupReplication.json' + path, formData)
  }

  backupTenant(path) {
    let formData = new FormData();
    return updateWithForm('/admin/backupTenant.json' + path, formData)
  }

  downloadBackupTenant(path) {
    return fetch('/admin/downloadBackupTenant.zip' + path + ".zip")
  }

  uploadBackupTenant(path, files, cb) {
    const config = {
      onUploadProgress: progressEvent => {
        const percentCompleted = Math.floor(
            (progressEvent.loaded * 100) / progressEvent.total);
        cb(percentCompleted)
      }
    }
    const data = new FormData()
    if(files.length > 0) {
      const file = files[0]
      data.append('file', file, file.name)
      data.append('force', 'true');
    }
    if (!data.entries().next().done) {
      return updateWithFormAndConfig('/admin/uploadBackupTenant.json' + path, data,
          config)
          .then(() => this.populateNodesForBrowser(path))
          .catch(error => {
//            log.error('Failed to upload: ' + error)
            reject('Unable to upload due to an error. ' + error)
          })
    }
    return
  }

  restoreTenant(path) {
    let formData = new FormData();
    return updateWithForm('/admin/restoreTenant.json' + path, formData)
  }
  
  acceptTermsAndConditions() {
    let formData = new FormData();
    return updateWithForm('/admin/acceptTermsAndConditions.json', formData).then( ()=> {
      return this.populateUser()
    })
  }

}

export default PerAdminImpl
