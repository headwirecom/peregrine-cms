<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
    <div class="explorer-preview-content preview-page">
        <template v-if="currentObject">
            <ul class="explorer-preview-nav">
                <template v-if="allowOperations">
                    <li>
                        <a  href="#!" 
                            title="rename page" 
                            class="waves-effect waves-light" 
                            v-on:click.stop.prevent="renamePage">
                            <admin-components-iconrename></admin-components-iconrename>
                        </a>
                    </li>
                    <li>
                        <a 
                            href="#!" 
                            title="move page" 
                            class="waves-effect waves-light" 
                            v-on:click.stop.prevent="movePage">
                            <i class="material-icons">compare_arrows</i>
                        </a>
                    </li>
                    <li>
                        <a 
                            href="#!" 
                            title="delete page" 
                            class="waves-effect waves-light" 
                            v-on:click.stop.prevent="deletePage">
                            <i class="material-icons">delete</i>
                        </a>
                    </li>
                </template>
                <li>
                    <a  v-if="edit"
                        title="cancel edit"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="onCancel">
                        <i class="material-icons">info</i>
                    </a>
                    <a  v-else
                        title="edit page properties"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="onEdit">
                        <i class="material-icons">edit</i>
                    </a>
                </li>

            </ul>
            <vue-form-generator 
                v-if="!edit"
                class="vfg-preview"
                v-on:validated = "onValidated"
                v-bind:schema  = "readOnlySchema"
                v-bind:model   = "page"
                v-bind:options = "options">
            </vue-form-generator>
            <template v-else>
                <vue-form-generator 
                    v-bind:schema="schema"
                    v-bind:model="page"
                    v-bind:options="options">
                </vue-form-generator>
                <button 
                    type="button"
                    title="save page properties"
                    v-bind:disabled="!valid"
                    class="btn btn-raised waves-effect waves-light right"
                    v-on:click.stop.prevent="onOk">
                    <i class="material-icons">check</i>
                </button>
            </template>
        </template>
        <div v-else class="explorer-preview-empty">
            <span>{{$i18n('no page selected')}}</span>
            <i class="material-icons">info</i>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            edit() {
                return $perAdminApp.getNodeFromView('/state/tools').edit
            },
            readOnlySchema() {
                if(!this.schema) return {}
                const roSchema = JSON.parse(JSON.stringify(this.schema))
                roSchema.fields.forEach( (field) => {
                    field.preview = true
                    if(field.fields) {
                        field.fields.forEach( (field) => {
                            field.preview = true
                        })
                    }
                })
                return roSchema

            },
            currentObject() {
                return $perAdminApp.getNodeFromViewOrNull("/state/tools/page")
            },
            page() {
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject)
            },
            allowOperations() {
                return this.currentObject.split('/').length > 4
            },
            schema() {
                const view = $perAdminApp.getView()
                if(this.page) {
                    const component = this.page.component
                    const schema = view.admin.componentDefinitions[component]
                    return schema
                }
            },

        },
        data: function() {
            return {
                options: {
                    validateAfterLoad: true,
                    validateAfterChanged: true,
                    focusFirstField: true
                }
            }
        },
        methods: {
            onEdit: function() {
                Vue.set($perAdminApp.getNodeFromView('/state/tools'), 'edit', true)
            },
            onValidated(isValid, errors) {
                this.valid = isValid
            },
            renamePage() {
                let newName = prompt('new name for '+this.page.name)
                if(newName) {
                    $perAdminApp.stateAction('renamePage', { path: this.page.path, name: newName})
                    let newPath = this.currentObject.split('/')
                    newPath.pop()
                    newPath.push(newName)
                    $perAdminApp.stateAction('showPageInfo', { selected: newPath.join('/') })
                }
            },
            deletePage() {
                $perAdminApp.stateAction('deletePage', this.page.path)
//                $perAdminApp.stateAction('showPageInfo', { selected: null })
            },
            movePage() {
                const root = '/content/sites'
                const type = 'folder'
                const pagePath = this.page.path
                const selectedPath = pagePath.substr(0, pagePath.lastIndexOf('/'))
                let currentPath
                // is selectedPath the root dir?
                selectedPath === root 
                    ? currentPath = selectedPath
                    : currentPath = selectedPath.substr(0, selectedPath.lastIndexOf('/'))
                const initModalState = {
                    root: root,
                    type: type,
                    current: currentPath,
                    selected: selectedPath
                }
                const options = {
                    complete: () => {
                        const newPath = $perAdminApp.getNodeFromView('/state/pathbrowser/selected')
                        $perAdminApp.stateAction('movePage', { path: pagePath, to: newPath, type: 'child'})
                        $perAdminApp.getNodeFromView('/state/tools').pages = newPath
                        $perAdminApp.getNodeFromView('/state/tools').page = null
                    }
                }
                $perAdminApp.pathBrowser(initModalState, options)
            },
            onCancel: function() {
                $perAdminApp.stateAction('showPageInfo', { selected: this.page.path  })
                $perAdminApp.getNodeFromView('/state/tools').edit = false
            },
            onOk() {
                $perAdminApp.stateAction('savePageProperties', this.page )
                $perAdminApp.getNodeFromView('/state/tools').edit = false
            }
        }
    }
</script>
