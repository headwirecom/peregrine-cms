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
    <div class="explorer-preview-content preview-object">
        <template v-if="currentObject && schema">
            <ul class="explorer-preview-nav">
                <li>
                    <a  href="#!"
                        title="rename object"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="renameObject">
                        <admin-components-iconrename></admin-components-iconrename>
                    </a>
                </li>
                <!--<li>-->
                    <!--<a  href="#!"-->
                        <!--title="move object"-->
                        <!--class="waves-effect waves-light"-->
                        <!--v-on:click.stop.prevent="moveObject">-->
                        <!--<i class="material-icons">compare_arrows</i>-->
                    <!--</a>-->
                <!--</li>-->
                <li>
                    <a  href="#!"
                        title="delete object"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="deleteObject">
                        <i class="material-icons">delete</i>
                    </a>
                </li>
                <li>
                    <a  v-if="!edit"
                        title="edit object" 
                        class="waves-effect waves-light" 
                        v-on:click.stop.prevent="onEdit">
                        <i class="material-icons">edit</i>
                    </a>
                    <a  v-else
                        title="preview object" 
                        class="waves-effect waves-light" 
                        v-on:click.stop.prevent="onCancel">
                        <i class="material-icons">info</i>
                    </a>
                </li>
            </ul>
            <template v-if="(edit === false || edit === undefined) && schema !== undefined">
                <template v-if="currentObject.data && schema">
                    <vue-form-generator 
                            class="vfg-preview"
                            v-on:validated = "onValidated"
                            v-bind:schema  = "readOnlySchema"
                            v-bind:model   = "currentObject.data"
                            v-bind:options = "formOptions">
                    </vue-form-generator>
                </template>
    <!--
                <div class="row" v-for="field in schema.fields">
                    <template v-if="!field.fields">
                        <div class="col s4"><b>{{field.label}}:</b></div><div class="col s8">{{get(currentObject.data,field.model)}}</div>
                    </template>
                    <template v-else>
                        <div class="col s12"><b>{{field.title}}</b></div>
                        <div class="row" v-for="item in currentObject.data[field.model]">
                            <div v-for="child in field.fields">
                                <div class="col s4"><b>{{child.label}}:</b></div><div class="col s8">{{get(item,child.model)}}</div>
                            </div>
                        </div>
                    </template>
                </div>
    -->
            </template>

            <template v-if="edit && currentObject.data && schema">
                <vue-form-generator
                  v-on:validated = "onValidated"
                  v-bind:schema  = "schema"
                  v-bind:model   = "currentObject.data"
                  v-bind:options = "formOptions">
                </vue-form-generator>
                <div class="explorer-confirm-dialog">
                    <button  
                        v-if="edit"
                        type="button"
                        title="save object" 
                        v-bind:disabled="!valid" 
                        class="btn btn-raised waves-effect waves-light right" 
                        v-on:click.stop.prevent="onOk">
                        <i class="material-icons">check</i>
                    </button>
                </div>
            </template>
        </template>

        <div v-if="currentObject === undefined || schema === undefined" class="explorer-preview-empty">
            <span>no object selected</span>
            <i class="material-icons">info</i>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
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
          currentObject: function () {
            return $perAdminApp.getNodeFromView("/state/tools/object")
          }, 
          schema: function () {
            if(!this.currentObject || !this.currentObject.data) return
            let resourceType = this.currentObject.data['component'] ? this.currentObject.data['component'] : this.currentObject.data['sling:resourceType']
            resourceType = resourceType.split('/').join('-')
            return $perAdminApp.getNodeFromView('/admin/componentDefinitions/' + resourceType)
          },
          edit() {
              return $perAdminApp.getNodeFromView('/state/tools').edit
          }
        },
        data: function() {
          return {
            valid: false,
            formOptions: {
              validateAfterLoad: true,
              validateAfterChanged: true,
              focusFirstField: true
            }
          }
        },
        methods: {
            get(obj, path) {
                const segments = path.split('.')
                if(segments.length == 1) {
                    return obj[segments[0]]
                } else {
                    if(obj[segments[0]]) {
                        return obj[segments[0]][segments[1]]
                    }
                }
                return '-'
            },
            onValidated(isValid, errors) {
                this.valid = isValid
            },
          onEdit: function() {
              $perAdminApp.stateAction('editObject', { selected: this.currentObject.show, path: '/state/tools/objects' })
              Vue.set($perAdminApp.getNodeFromView('/state/tools'), 'edit', true)
          },
          onOk: function() {
              // should store the current node
              $perAdminApp.stateAction('saveObjectEdit', { data: this.currentObject.data, path: this.currentObject.show })
              $perAdminApp.stateAction('selectObject', { selected: this.currentObject.show })
          },
          onCancel: function() {
            $perAdminApp.stateAction('selectObject', { selected: this.currentObject.show })
          },
            renameObject() {
                const name = $perAdminApp.getNodeFromView('/state/tools/object').show.substring($perAdminApp.getNodeFromView('/state/tools/object').show.lastIndexOf('/')+1)
                let newName = prompt('new name for '+name)
                if(newName) {
                    $perAdminApp.stateAction('renameObject', { path: $perAdminApp.getNodeFromView('/state/tools/object').show, name: newName})
                    $perAdminApp.getNodeFromView('/state/tools').object = null
                }
            },
            deleteObject() {
                const really = confirm("Are you sure you want to delete this object and all its children?")
                if(really) {
                    $perAdminApp.stateAction('deleteObject', $perAdminApp.getNodeFromView('/state/tools/object').show)
                }
            },
            moveObject() {
                const root = '/content/objects'
                const type = 'folder'
                const assetPath = $perAdminApp.getNodeFromView('/state/tools/object').show
                const selectedPath = assetPath.substr(0, assetPath.lastIndexOf('/'))
                console.log(selectedPath)
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
                console.log(initModalState)
                const options = {
                    complete: () => {
                        const newPath = $perAdminApp.getNodeFromView('/state/pathbrowser/selected')
                        $perAdminApp.stateAction('moveObject', { path: assetPath, to: newPath, type: 'child'})
                        $perAdminApp.getNodeFromView('/state/tools').objects = newPath
                        $perAdminApp.getNodeFromView('/state/tools').object = null
                    }
                }
                $perAdminApp.pathBrowser(initModalState, options)
            },
        }
    }
</script>
