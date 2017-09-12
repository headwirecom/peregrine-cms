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
    <div class="explorer-preview-content preview-asset">
        <template v-if="currentObject">
            <ul class="explorer-preview-nav">
                <li>
                    <a  href="#!"
                        title="rename asset"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="renameAsset">
                        <admin-components-iconrename></admin-components-iconrename>
                    </a>
                </li>
                <li>
                    <a  href="#!"
                        title="move asset"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="moveAsset">
                        <i class="material-icons">compare_arrows</i>
                    </a>
                </li>
                <li>
                    <a  href="#!"
                        title="delete asset"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="deleteAsset">
                        <i class="material-icons">delete</i>
                    </a>
                </li>
                <li>
                    <a  v-if="!edit"
                        href="#!"
                        title="edit"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="onEdit">
                        <i class="material-icons">edit</i>
                    </a>
                    <a  v-else
                        href="#!"
                        title="edit"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="onCancel">
                        <i class="material-icons">info</i>
                    </a>
                </li>
                <li>
                    <a  href="#!"
                        title="references"
                        class="waves-effect waves-light"
                        v-on:click.stop.prevent="onReferences">
                        <i class="material-icons">list</i>
                </li>
            </ul>
            <template v-if="!edit && !references">
                <img v-if="isImage(currentObject.show)" v-bind:src="currentObject.show" style="margin-top: 1em;"/>
                <iframe v-else v-bind:src="currentObject.show" style="width: 100%; height: 60%; margin-top: 1em;"></iframe>

                <vue-form-generator
                        class="vfg-preview"
                        v-on:validated = "onValidated"
                        v-bind:schema  = "readOnlySchema"
                        v-bind:model   = "asset"
                        v-bind:options = "options">
                </vue-form-generator>
            </template>

            <template v-if="edit">
                <vue-form-generator 
                    class="vfg-preview"
                    v-on:validated = "onValidated"
                    v-bind:schema  = "schema"
                    v-bind:model   = "asset"
                    v-bind:options = "options">
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

            <ul v-if="references && !edit" class="collection with-header">
                <li class="collection-header"><h5>Referenced By</h5></li>
                <li class="collection-item" v-for="item in referencedBy">{{item.path}}</li>
            </ul>

        </template>
        <div v-else class="explorer-preview-empty">
            <span>no asset selected</span>
            <i class="material-icons">info</i>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        data: function() {
            return {
                edit: false,
                references: false,
                valid: true,
                options: {
                    validateAfterLoad: true,
                    validateAfterChanged: true,
                    focusFirstField: true
                }
            }
        },
        computed: {
            schema() {
                const view = $perAdminApp.getView()
                if(this.asset) {
                    return { "fields":[
                        {
                            "type": "input",
                            "inputType": "text",
                            "model": "name",
                            "label": "Name",
                            "readonly": true,
                            "placeholder": "page name"
                        },
                        {
                            "type": "input",
                            "inputType": "text",
                            "model": "title",
                            "x-model": "jcr:title",
                            "label": "Title",
                            "placeholder": "asset title"
                        },
                        {
                            "type": "material-datetime",
                            "inputType": "text",
                            "model": "created",
                            "label": "Created",
                            "readonly": true,
                            "placeholder": "created"
                        },
                        {
                            "type": "input",
                            "inputType": "text",
                            "model": "createdBy",
                            "label": "Created By",
                            "readonly": true,
                            "placeholder": "created by"
                        },
                        {
                            "type": "material-datetime",
                            "inputType": "text",
                            "model": "lastModified",
                            "label": "Last Modified",
                            "readonly": true,
                            "placeholder": "lastModified"
                        },
                        {
                            "type": "input",
                            "inputType": "text",
                            "model": "lastModifiedBy",
                            "label": "Last Modified By",
                            "readonly": true,
                            "placeholder": "lastModifiedBy"
                        },
                        {
                            "type": "vueMultiSelect",
                            "model": "tags",
                            "label": "Tags",
                            "placeholder": "tags",
                            "selectOptions": {
                                "multiple": true,
                                "trackBy": "name",
                                "label": "name",
                                "searchable": true,
                                "clearOnSelect": true,
                                "closeOnSelect": false,
                                "taggable": true
                            },
                            "valuesFrom": "/content/objects/tags.infinity.json",
                            "values": []
                        },
                        {
                            "type": "material-textarea",
                            "inputType": "text",
                            "model": "description",
                            "label": "Description",
                            "rows": 10,
                            "placeholder": "enter a description for this asset"
                        }
                    ]}

                }
            },
            referencedBy() {
                return $perAdminApp.getView().state.referencedBy.referencedBy
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
            currentObject: function () {
                return $perAdminApp.getNodeFromView("/state/tools/asset")
            },
            asset: function() {
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject.show)
            },

        },
        methods: {
            isImage: function(path) {
                const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, path)
                if(!node) return false
                const mime = node.mimeType
                return ['image/png','image/jpeg','image/jpg','image/gif','timage/tiff', 'image/svg+xml'].indexOf(mime) >= 0
            },
            renameAsset() {
                let newName = prompt('new name for '+this.asset.name)
                if(newName) {
                    $perAdminApp.stateAction('renameAsset', { path: this.asset.path, name: newName})
                    $perAdminApp.getNodeFromView('/state/tools').asset = null
                }
            },
            deleteAsset() {
                $perAdminApp.stateAction('deleteAsset', this.asset.path)
            },
            moveAsset() {
                const root = '/content/assets'
                const type = 'folder'
                const assetPath = this.asset.path
                const selectedPath = assetPath.substr(0, assetPath.lastIndexOf('/'))
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
                        $perAdminApp.stateAction('moveAsset', { path: assetPath, to: newPath, type: 'child'})
                        $perAdminApp.getNodeFromView('/state/tools').assets = newPath
                        $perAdminApp.getNodeFromView('/state/tools').asset = null
                    }
                }
                $perAdminApp.pathBrowser(initModalState, options)
            },
            onEdit() {
                this.edit = true
                this.references = false
            },
            onCancel() {
                this.edit = false
                this.references = false
                $perAdminApp.stateAction('selectAsset', { selected: this.asset.path  })
            },
            onValidated(isValid, errors) {
                this.valid = isValid
            },
            onOk() {
                $perAdminApp.stateAction('saveAssetProperties', this.asset )
                this.edit = false
                this.references = false
            },
            onReferences() {
                this.edit = false
                this.references = true
            }

        }
    }
</script>
