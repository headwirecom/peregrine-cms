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
        <div v-if="isVisible" class="modal-container">
            <div id="pathBrowserModal" class="modal default">
                <div class="modal-content">
                    <div class="row">
                        <div class="col s6">
                            <ul class="tabs">
                                <li class="tab col s4"><a href="#" v-on:click="select('browse')">Browse</a></li>
                                <li class="tab col s4"><a href="#" v-on:click="select('cards')">Cards</a></li>
                                <li class="tab col s4"><a href="#">
                                    <input placeholder="search"  type="text" v-model="search">
                                </a></li>
                            </ul>
                            <div class="col s12" v-if="tab === 'browse'">
                                <div v-if="!search" class="collection" style="height: 300px; overflow: scroll;">
                                    <a href="#!" v-on:click.stop.prevent="selectParent" class="collection-item">{{path}}</a>
                                    <template v-for="item in nodes.children">
                                        <a class="collection-item" v-if="isFolder(item)" v-on:click.stop.prevent="selectFolder(item)">[] {{item.name}}</a>
                                        <a class="collection-item" v-if="isFile(item)" v-on:click.stop.prevent="selectItem(item)">[F] {{item.name}}</a>
                                    </template>
                                </div>
                                <div v-else style="height: 300px; overflow: scroll;">
                                    <table >
                                        <thead>
                                        <tr>
                                            <th>Type</th>
                                            <th>Name</th>
                                            <th>Path</th>
                                        </tr>
                                        </thead>

                                        <tbody>
                                        <tr v-for="item in nodes.children" v-if="searchFilter(item)">
                                            <td>{{item.mimeType}}</td>
                                            <td>{{item.name}}</td>
                                            <td>{{item.path}}</td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="col s12" v-if="tab === 'cards'">
                                <form action="#">
                                    <p class="range-field">
                                        <input type="range" id="test5" min="2" max="5" v-model="cardSize"/>
                                    </p>
                                </form>
                                <div class="row" style="height: 300px; overflow: scroll;">
                                    <div class="col" v-bind:class="cardClass" >
                                        <div class="card" v-bind:style="cardStyle" v-on:click.stop.prevent="selectParent()">
                                            <div class="card-image active">
                                                <div>..</div>
                                            </div>
                                        </div>
                                    </div>
                                    <template v-for="item in nodes.children" v-if="searchFilter(item)">
                                        <div class="col" v-bind:class="cardClass" >
                                            <div class="card" v-bind:style="cardStyle" v-if="isFolder(item)" v-bind:src="item.path" v-on:click.stop.prevent="selectFolder(item)">
                                                <div class="card-image active">
                                                    <div>{{item.name}}</div>
                                                </div>
                                            </div>
                                            <div class="card" v-bind:style="cardStyle" v-if="isFile(item)" v-bind:src="item.path" v-on:click.stop.prevent="selectFolder(item)">
                                                <div class="card-image active">
                                                    <img v-if="isFile(item)" v-bind:src="item.path" v-on:click.stop.prevent="selectItem(item)">
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                </div>
                            </div>
                            <div class="col s12" v-if="tab === 'search'">
                                <input placeholder="search"  type="text" value="">
                            </div>
                        </div>
                        <div class="col s6">
                            <h6>Preview</h6>
                            <div style="max-height: 300px; width: 100%; overflow: scroll;">
                                <img v-bind:src="preview" style="max-width: 100%">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="row">
                        <div class="col s10">
                            <input type="text" v-bind:value="preview">
                        </div>
                        <div class="col s2">
                            <button v-on:click="onHide">cancel</button>
                            <button v-on:click="onOk">select</button>
                        </div>
                    </div>
                    <!--
                    <button
                        type="button"
                        v-on:click="onOk"
                        class="modal-action modal-close waves-effect waves-light btn-flat">
                        ok
                    </button>
                    -->
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
                let root = $perAdminApp.getNodeFromViewOrNull('/state/assetbrowser/root')
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
                return $perAdminApp.getNodeFromViewOrNull('/state/assetbrowser/isVisible')
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
                    let pb = $perAdminApp.getNodeFromView('/state/assetbrowser')
                    pb.root = item.path
                })
            },
            selectItem(item) {
                this.preview = item.path
            },
            setItemPath(path){
                return $perAdminApp.getNodeFromViewOrNull('/state/assetbrowser/methods').setItemPath(path)
            },
            onHide() {
                return $perAdminApp.getNodeFromViewOrNull('/state/assetbrowser/methods').onHide()
            },
            onOk() {
                $perAdminApp.getNodeFromViewOrNull('/state/assetbrowser/methods')
                    .setItemPath(this.preview)
                this.onHide()
            }
        }
    }
</script>
