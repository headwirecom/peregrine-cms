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
                <ul class="pathbrowser-tabs">
                    <li class="tab">
                        <a href="#" :class="tab === 'browse' ? 'active' : ''" v-on:click="select('browse')">
                            <i class="material-icons">list</i>
                        </a>
                    </li>
                    <li class="tab">
                        <a href="#" :class="tab === 'cards' ? 'active' : ''" v-on:click="select('cards')">
                            <i class="material-icons">view_module</i>
                        </a>
                    </li>
                    <li 
                        class="indicator" 
                        :style="`transform: translateX(${tab === 'browse' ? '0' : '72px'})`">
                    </li>
                </ul>
                <div class="pathbrowser-filter">
                    <input placeholder="search" 
                           type="search" 
                           v-model="search" />
                </div>
                <div class="modal-content">
                    <div class="col-browse"> 
                        <div v-if="search">
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
                        <template v-if="tab === 'browse' && !search"">
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
                        </template>
                        <template v-if="tab === 'cards' && !search">
                            <template v-if="list.length > 0">
                                <ul class="cards-toolbar sort-nav">
                                    <li>
                                        <span class="cards-toolbar-title">Sort</span>
                                    </li>
                                    <li>
                                        <input 
                                            name="pagebrowser_sort_cards" 
                                            type="radio" 
                                            class="with-gap" 
                                            id="pagebrowser_sort_cards_name" 
                                            :checked="sortBy === 'name'"/>
                                        <label v-on:click="onSort('name')" for="pagebrowser_sort_cards_name">name</label>
                                    </li>
                                    <li>
                                        <input 
                                            name="pagebrowser_sort_cards" 
                                            type="radio" 
                                            class="with-gap" 
                                            id="pagebrowser_sort_cards_date" 
                                            :checked="sortBy === 'created'"/>
                                        <label v-on:click="onSort('created')" for="pagebrowser_sort_cards_date">date</label>
                                    </li>
                                </ul>
                            
                                <p class="range-field">
                                    <input 
                                        type="range" 
                                        min="120" 
                                        max="400" 
                                        v-model="cardSize"/>
                                </p>
                                <!--<admin-components-spinner 
                                    v-if="isotopeLoading"
                                    width="60" 
                                    position="center">
                                </admin-components-spinner> -->
                                
                                <isotope 
                                    ref="isotope" 
                                    class="isotopes" 
                                    v-bind:options="getIsotopeOptions()"
                                    v-images-loaded:on="getImagesLoadedCbs()" 
                                    v-bind:list="list">
                                    <div 
                                        v-for="(item, index) in list" 
                                        :key="item.path">
                                        <div 
                                            v-if="isFolder(item)" 
                                            class="item-folder"
                                            v-bind:style="`width: ${cardSize}px; height: ${cardSize}px`"
                                            v-on:click.stop.prevent="selectFolder(item)">
                                                <div class="item-content">
                                                    <i 
                                                        class="material-icons"
                                                        :style="`font-size: ${cardIconSize(cardSize)}px`">folder_open</i>
                                                    <br/>{{item.name}}
                                                </div>
                                        </div>
                                    </div>
                                </isotope>
                            </template>
                            <p v-else class="flow-text">This folder is empty.</p>
                        </template>
                    </div>
                    <div class="col-preview">
                        <template v-if="preview">
                            <div v-if="isFolder(preview)" class="preview-folder">
                                <i class="material-icons">folder_open</i>
                            </div>
                            <img v-else class="preview-image" v-bind:src="preview.path">
                            <dl class="preview-data">
                                <dt>Name</dt>
                                <dd>{{preview.name}}</dd>
                                <dt>Type</dt>
                                <dd>{{preview.resourceType}}</dd>
                                <dt>Path</dt>
                                <dd>{{preview.path}}</dd>
                                <dt>Created</dt>
                                <dd>{{preview.created}}</dd>
                            </dl>
                        </template>
                        <div v-else class="no-asset-selected">
                            <span>no asset selected</span>
                            <i class="material-icons">info</i>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <span class="current-folder">
                        <button 
                            :disabled="path === '/content'" 
                            type="button" 
                            class="btn-flat" 
                            v-on:click.stop.prevent="selectParent">
                            <i class="material-icons">keyboard_arrow_left</i> 
                        </button>
                        {{path}} ({{list.length}})
                    </span>
                    <button 
                        v-on:click="onHide" 
                        class="modal-action modal-close waves-effect waves-light btn-flat">cancel</button>
                    <button 
                        v-on:click="onOk" 
                        class="modal-action modal-close waves-effect waves-light btn-flat">select</button>
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
                cardSize: 120,
                search: '',
                preview: ''
            }
        },
        watch: {
            cardSize: function (newCardSize) {
                this.updatedIsotopeLayout('masonry')
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
            list(){
                console.log('list: ', this.nodes.children)
                return this.nodes.children || []
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
            cardIconSize: function(cardSize){
                return Math.floor(cardSize/3)
            },
            getImagesLoadedCbs: function() {
              return {
                progress: (instance, img ) => {
                  console.log('progress')
                },
                always: (instance) => {
                  console.log('always')
                },
                done: (instance) => {
                  console.log('done')
                },
                fail: (instance) => {
                  console.log('fail')
                }
              }
            },
            updatedIsotopeLayout: function(layout){
                this.$refs.isotope.layout(layout)
            },
            getIsotopeOptions: function() {
                return {
                    layoutMode: 'masonry',
                    itemSelector: '.card',
                    stamp: '.stamp',
                    masonry: {
                        gutter: 15
                    },
                    getSortData: {
                        name: function(itemElem){
                            return itemElem.name.toLowerCase()    
                        },
                        created: function(itemElem){
                            return Date.parse(itemElem.created)
                        }
                    }
                }
            },

            onSort(sortType){
                this.sortBy = sortType
                this.$refs.isotope.sort(sortType)
            },

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
                    if(this.tab === 'cards'){
                        this.updatedIsotopeLayout('masonry')
                    }
                    this.preview = item
                })
            },
            selectItem(item) {
                this.preview = item
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
