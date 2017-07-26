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
    <transition name="fade">
        <div v-if="isVisible" class="pathbrowser pagebrowser modal-container">
            <div id="pageBrowserModal" class="modal default modal-fixed-footer">
                <div class="modal-content">
                    <h4>Page Browser</h4>
                    <input placeholder="search" 
                           type="text" 
                           v-model="search" 
                           style="width: 100%" />
                    <template v-if="tab === 'browse'">
                        <h6>{{path}}</h6>
                        <ul v-if="!search" class="browse-list">
                            <li v-on:click.stop.prevent="selectParent">
                                <i class="material-icons">folder</i> <label>..</label>
                            </li>
                            <li v-if="isFolder(item)" 
                                v-for="item in nodes.children" 
                                v-on:click.stop.prevent="selectFolder(item)">
                                <i class="material-icons">folder</i>
                                <label>{{item.name}}</label>
                            </li>
                        </ul>
                        <div v-else>
                            <table >
                                <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Path</th>
                                </tr>
                                </thead>

                                <tbody>
                                <tr v-for="item in nodes.children" v-if="searchFilter(item)">
                                    <td>{{item.name}}</td>
                                    <td>{{item.path}}</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </template>
                    <input v-if="tab === 'search'" 
                           placeholder="search"  
                           type="text" 
                           value="" />
                </div>
                <div class="modal-footer">

                    <!-- <input type="text" v-bind:value="preview"> -->

                    <button v-on:click="onHide" class="modal-action modal-close waves-effect waves-light btn-flat">cancel</button>
                    <button v-on:click="onOk" class="modal-action modal-close waves-effect waves-light btn-flat">select</button>
                </div>
            </div>
            <div v-on:click="onHide" class="modal-overlay"></div>
        </div>
    </transition>
</template>

<script>
    export default {
        props: ['model'],
        data: function() {

            return {
                tab: 'browse',
                cardSize: 2,
                search: '',
                preview: ''
            }
        },
        computed: {
            path() {
                let root = $perAdminApp.getNodeFromViewOrNull('/state/pagebrowser/root')
                return root
            },
            nodes() {
                let view = $perAdminApp.getView()
                let nodes = view.admin.pathBrowser
                if(nodes && this.path) {
                    return $perAdminApp.findNodeFromPath(nodes, this.path)
                }
                return {}
            },
            isVisible() {
                return $perAdminApp.getNodeFromViewOrNull('/state/pagebrowser/isVisible')
            },
            cardClass() {
                return 's' + this.cardSize
            },
            cardStyle() {
                const pix = [20, 40,60,80,100,120,140][this.cardSize]
                return 'height: '+pix+'px; overflow: hidden'
            }
        },
        methods: {
            select(name) {
                this.tab = name
            },
            searchFilter(item) {
                if(this.search.length === 0) return true
                return (item.name.indexOf(this.search) >= 0)
            },
            selectParent() {
                let parentFolder = this.path.split('/')
                parentFolder.pop()
                let newPath = parentFolder.join('/')
                this.selectFolder({ path: newPath} )
            },
            display(item) {
                return item.name !== 'jcr:content'
            },
            isFile(item) {
                return ['per:Asset','nt:file'].indexOf(item.resourceType) >= 0
            },
            isFolder(item) {
                return ['per:Page','nt:folder', 'sling:Folder', 'sling:OrderedFolder'].indexOf(item.resourceType) >= 0
            },
            isSelected(name) {
                return name === this.selected
            },
            selectFolder(item) {
                $perAdminApp.getApi().populateNodesForBrowser(item.path, 'pathBrowser').then( () => {
                    let pb = $perAdminApp.getNodeFromView('/state/pagebrowser')
                    pb.root = item.path
                    this.preview = item.path
                })
            },
            selectItem(item) {
                this.preview = item.path
            },
            setItemPath(path){
                return $perAdminApp.getNodeFromViewOrNull('/state/pagebrowser/methods').setItemPath(path)
            },
            onHide() {
                return $perAdminApp.getNodeFromViewOrNull('/state/pagebrowser/methods').onHide()
            },
            onOk() {
                $perAdminApp.getNodeFromViewOrNull('/state/pagebrowser/methods')
                    .setItemPath(this.preview)
                this.onHide()
            }
        }
    }
</script>
