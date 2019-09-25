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
                <div class="explorer-confirm-dialog">
                    <button
                        type="button"
                        title="save page properties"
                        v-bind:disabled="!valid"
                        class="btn btn-raised waves-effect waves-light right"
                        v-on:click.stop.prevent="onOk">
                        <i class="material-icons">check</i>
                    </button>
                </div>
            </template>
        </template>
        <div v-else class="explorer-preview-empty">
            <span>{{$i18n('no page selected')}}</span>
            <i class="material-icons">info</i>
        </div>

    <admin-components-pathbrowser
            v-if="isOpen"
            :isOpen="isOpen"
            :browserRoot="browserRoot"
            :browserType="'page'"
            :currentPath="currentPath"
            :selectedPath="selectedPath"
            :setCurrentPath="setCurrentPath"
            :setSelectedPath="setSelectedPath"
            :onCancel="onMoveCancel"
            :onSelect="onMoveSelect">
    </admin-components-pathbrowser>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        updated: function() {
            let stateTools = $perAdminApp.getNodeFromView("/state/tools");
            stateTools._deleted = {};
        },
        computed: {
            edit() {
                return $perAdminApp.getNodeFromView('/state/tools').edit
            },
            readOnlySchema() {
                if(!this.schema) return {}
                const roSchema = JSON.parse(JSON.stringify(this.schema))
                roSchema.fields.forEach( (field) => {
                    field.preview = true
                    field.readonly = true
                    if(field.fields) {
                        field.fields.forEach( (field) => {
                            field.preview = true
                            field.readonly = true
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
                isOpen: false,
                browserRoot: '/content/sites',
                currentPath: '/content/sites',
                selectedPath: null,
                valid: true,
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
                let me = this
                $perAdminApp.promptUser('Rename Page', 'What is the new name for this page?', {
                    yesText: 'Rename',
                    yes(newName) {
                        if(newName) {
                            let renamePromise = $perAdminApp.stateAction('renamePage', { path: me.page.path, name: newName})
                            let newPath = me.currentObject.split('/')
                            renamePromise.then(
                                function(a,b,c,d) {
                                    newPath.pop()
                                    newPath.push(newName)
                                    $perAdminApp.stateAction('showPageInfo', { selected: newPath.join('/') })
                                        .then(() => {});
                                }
                            );
                        }
                    }
                })
            },
            deletePage() {
                $perAdminApp.stateAction('deletePage', this.page.path)
//                $perAdminApp.stateAction('showPageInfo', { selected: null })
            },
            setCurrentPath(path){
                this.currentPath = path
            },
            setSelectedPath(path){
                this.selectedPath = path
            },
            movePage() {
                $perAdminApp.getApi().populateNodesForBrowser(this.currentPath, 'pathBrowser')
                    .then( () => {
                        this.isOpen = true
                    }).catch( (err) => {
                    $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
                })
            },
            onMoveCancel(){
                this.isOpen = false
            },
            onMoveSelect() {
                $perAdminApp.stateAction('movePage', { path: this.page.path, to: this.selectedPath, type: 'child'})
                $perAdminApp.stateAction('unselectPage', { })
                this.isOpen = false
            },
            onCancel: function() {
                $perAdminApp.stateAction('showPageInfo', { selected: this.page.path  })
                $perAdminApp.getNodeFromView('/state/tools').edit = false
            },
            onOk() {
                let data = JSON.parse(JSON.stringify(this.page));
                let _deleted = $perAdminApp.getNodeFromView("/state/tools/_deleted");

                //Merge _deleted child items back into the object that we need to save.
                //Loop through the model for this object/page/asset and find objects that have children
                for ( const key in data) {
                    //If data[key] or deleted[key] is an array of objects
                    if (( Array.isArray(data[key]) && data[key].length && typeof data[key][0] === 'object') || 
                        ( Array.isArray(_deleted[key]) && _deleted[key].length && typeof _deleted[key][0] === 'object') ) {

                        let node = data[key];

                        //Use an object to easily merge back in deleted nodes
                        let targetNode = {}
                        //Insert deleted children
                        for ( const j in _deleted[key]) {
                            const deleted = _deleted[key][j]
                            targetNode[deleted.name] = deleted;
                        }
                        //Insert children
                        for ( const i in node ) {
                            const child = node[i]
                            targetNode[child.name] = child;
                        }
                        data[key] = Object.values(targetNode);
                    }
                }
                $perAdminApp.stateAction('savePageProperties', data)
                $perAdminApp.getNodeFromView('/state/tools').edit = false
            }
        }
    }
</script>
