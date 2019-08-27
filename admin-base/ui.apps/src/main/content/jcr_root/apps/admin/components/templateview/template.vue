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
    <div class="explorer-preview-content preview-template">
        <template v-if="currentObject">
            <ul class="explorer-preview-nav">
                <template v-if="allowOperations">
                    <li>
                        <a  href="#!"
                            title="rename template"
                            v-on:click.stop.prevent="renamePage">
                            <i class="svg-icons svg-icon-rename"></i>
                        </a>
                    </li>
                    <li>
                        <a  href="#!"
                            title="move template"
                            v-on:click.stop.prevent="movePage">
                            <i class="material-icons">compare_arrows</i>
                        </a>
                    </li>
                    <li>
                        <a  href="#!"
                            title="delete template"
                            v-on:click.stop.prevent="deletePage">
                            <i class="material-icons">delete</i>
                        </a>
                    </li>
                </template>
                <li>
                    <a href="#!"
                       v-bind:title="$i18n('cancel')"
                       v-on:click.stop.prevent="onCancel">
                        <i class="material-icons">close</i>
                    </a>
                </li>
                <li>
                    <a href="#!"
                       v-bind:title="$i18n('save')"
                       v-on:click.stop.prevent="onOk">
                        <i class="material-icons">check</i>
                    </a>
                </li>
            </ul>
            <vue-form-generator v-bind:schema="schema"
                                v-bind:model="page"
                                v-bind:options="options">
            </vue-form-generator>
        </template>
        <div v-else class="explorer-preview-empty">
            <span>{{ $i18n('no template selected') }}</span>
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
        computed: {
            currentObject() {
                return $perAdminApp.getNodeFromViewOrNull("/state/tools/template")
            },
            page() {
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject)
            },
            allowOperations() {
                return this.currentObject.split('/').length > 4
            },
            schema() {
                const view = $perAdminApp.getView()
                const component = this.page.component
                const schema = view.admin.componentDefinitions[component]
                return schema
            },

        },
        data: function() {
            return {
                isOpen: false,
                browserRoot: '/content/templates',
                currentPath: '/content/templates',
                selectedPath: null,
                options: {
                    validateAfterLoad: true,
                    validateAfterChanged: true,
                    focusFirstField: true
                }
            }
        },
        methods: {
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
                $perAdminApp.stateAction('showPageInfo', { selected: null })
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
                $perAdminApp.stateAction('moveTemplate', { path: this.page.path, to: this.selectedPath, type: 'child'})
                $perAdminApp.stateAction('unselectTemplate', { })
                this.isOpen = false
            },
            onCancel() {

            },
            onOk() {
                $perAdminApp.stateAction('savePageProperties', this.page )
            }
        }
    }
</script>
