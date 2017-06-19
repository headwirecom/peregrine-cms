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
<div>
    <transition name="fade">
        <div v-if="isVisible" id="pathBrowserModal" class="modal default">
            <div class="modal-content">
                <div class="row">
                    <div class="col s12">
                        <ul class="tabs">
                            <li class="tab col s2"><a href="#" v-bind:class="isSelected('browse') ? 'active' : ''" v-on:click.stop.prevent="selectBrowse">Browse</a></li>
                            <li class="tab col s2"><a href="#" v-bind:class="isSelected('search') ? 'active' : ''" v-on:click.stop.prevent="selectSearch">Search</a></li>
                        </ul>
                    </div>
                    <div v-if="isSelected('browse')" class="col s12" v-on:click.stop.prevent="selectParent">
                        <ul class="collection with-header">
                            <li class="collection-header">{{path}}</li>
                            <li class="collection-item" v-for="item in nodes.children" v-if="display(item)">
                                <a href="" v-if="isFile(item)" v-on:click.stop.prevent="selectItem(item)">{{item.name}}</a>
                                <a href="" v-if="isFolder(item)" v-on:click.stop.prevent="selectFolder(item)">{{item.name}}</a>
                            </li>
                        </ul>
                    </div>
                    <div v-if="isSelected('search')" class="col s12">
                        searchui
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <a 
                    v-on:click="onHide"
                    href="#!"
                    class="modal-action modal-close waves-effect waves-green btn-flat">
                    ok
                </a>
            </div>
        </div>
    </transition>
    <transition name="fade">
        <div v-if="isVisible" v-on:click="onHide" class="modal-overlay"></div>
    </transition>
</div>
</template>

<script>
    export default {
        props: ['model'],
        data: function() {
            return {
                selected: 'browse'
            }
        },
        computed: {
            path() {
                let root = $perAdminApp.getNodeFromViewOrNull('/state/pathbrowser/root')
                return root
            },
            nodes() {
                let view = $perAdminApp.getView()
                let nodes = view.admin.nodes
                if(nodes && this.path) {
                    return $perAdminApp.findNodeFromPath(nodes, this.path)
                }
                return {}
            },
            isVisible() {
                return $perAdminApp.getNodeFromViewOrNull('/state/pathbrowser/isVisible')
            }
        },
        methods: {
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
            selectBrowse(ev) {
                this.selected = 'browse'
            },
            selectSearch(ev) {
                this.selected = 'search'
            },
            selectFolder(item) {
                $perAdminApp.getApi().populateNodesForBrowser(item.path).then( () => {
                    let pb = $perAdminApp.getNodeFromView('/state/pathbrowser')
                    pb.root = item.path
                })
            },
            selectItem(item) {
                console.log(item)
                this.setItemPath(item.path)
                this.onHide()
            },
            setItemPath(path){
                return $perAdminApp.getNodeFromViewOrNull('/state/pathbrowser/methods').setItemPath(path)
            },
            onHide() {
                return $perAdminApp.getNodeFromViewOrNull('/state/pathbrowser/methods').onHide()
            }
        }
    }
</script>
