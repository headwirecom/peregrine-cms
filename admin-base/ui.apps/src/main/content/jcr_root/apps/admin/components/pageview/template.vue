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
    <div class="asset-preview">
        <template v-if="currentObject">
            <p/>
            <template v-if="allowOperations">
                <button class="btn" v-on:click.stop.prevent="renamePage()">rename</button>
                <button class="btn" v-on:click.stop.prevent="movePage()">move</button>
                <button class="btn" v-on:click.stop.prevent="deletePage()">delete</button>
            </template>
            <vue-form-generator v-bind:schema="schema"
                                v-bind:model="page"
                                v-bind:options="options">

            </vue-form-generator>
            <button class="waves-effect waves-light btn btn-raised" v-on:click.stop.prevent="onCancel">
                <i class="material-icons">close</button>
            <button class="waves-effect waves-light btn btn-raised" v-on:click.stop.prevent="onOk">
                <i class="material-icons">check</i></button>
        </template>
        <template v-else>
            <div class="no-asset-selected">
                <span>no page selected</span>
                <i class="material-icons">info</i>
            </div>
        </template>
    </div>

</template>

<script>
    export default {
        props: ['model'],
        computed: {
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
                const component = this.page.component
                const schema = view.admin.componentDefinitions[component]
                return schema
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
            movePage() {
                let path = this.page.path
                $perAdminApp.pathBrowser(
                    '/content/sites',
                    (newValue) => {
                        $perAdminApp.stateAction('movePage', { path: path, to: newValue, type: 'child'})
                        $perAdminApp.getNodeFromView('/state/tools').pages = newValue
                        $perAdminApp.getNodeFromView('/state/tools').page = null
                    }
                )
            },
            onCancel() {

            },
            onOk() {
                $perAdminApp.stateAction('savePageProperties', this.page )
            }
        }
    }
</script>
