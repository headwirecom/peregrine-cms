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
<transition name="modal">
    <div class="modal-mask" v-on:click.stop.prevent="onCancel">
        <div class="modal-wrapper">
        <div class="pathbrowser modal-container" v-on:click.stop.prevent="onPrevent">
            <div class="modal-header" v-if="">
                {{ header }}
            </div>
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
                <li v-if="withLinkTab" class="tab">
                    <a href="#" :class="tab === 'link' ? 'active' : ''" v-on:click="select('link')">
                        <i class="material-icons">link</i>
                    </a>
                </li>
                <li
                    class="indicator"
                    :style="`transform: translateX(${tabIndicatorPosition}px)`">
                </li>
            </ul>
            <div class="pathbrowser-filter" :style="`width: calc(100% - ${searchTabOffset}px)`">
                <input placeholder="search"  type="text" v-model="search">
            </div>
            <div class="modal-content">
                <div class="col-browse">
                    <table v-if="search" class="highlight pathbrowser-search-results">
                        <thead>
                            <tr>
                                <th></th>
                                <th>Type</th>
                                <th>Name</th>
                                <th>Path</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-if="searchFilter(item)"
                                v-for="item in nodes.children"
                                :class="isSelected(item.path) ? 'selected' : ''">
                                <td v-if="isSelectable(item)" class="search-radio">
                                    <input type="radio" class="with-gap" :checked="isSelected(item.path)" />
                                    <label v-on:click.stop.prevent="selectItem(item)"></label>
                                </td>
                                <td>{{item.mimeType || 'folder'}}</td>
                                <td>{{item.name}}</td>
                                <td>{{item.path}}</td>
                            </tr>
                        </tbody>
                    </table>
                    <template v-if="tab === 'browse' && !search">
                        <nav class="modal-content-nav clearfix">
                            <div class="modal-content-section">
                                <div class="current-folder">
                                    <template v-if="!isRoot">
                                        <a
                                            href="#!"
                                            v-on:click.stop.prevent="selectParent">
                                            <i class="material-icons">keyboard_arrow_left</i>
                                        </a>
                                        {{currentPath}} ({{list.length}})
                                    </template>
                                    <template v-else>
                                        <input
                                            v-if="!isBrowserTypeImage"
                                            type="radio"
                                            class="with-gap"
                                            :checked="isSelected(currentPath)"/>
                                        <label v-on:click.stop.prevent="selectItem(nodes)">{{currentPath}} ({{list.length}})</label>
                                    </template>
                                </div>
                            </div>
                        </nav>
                        <ul class="browse-list" v-if="list.length > 0">
                            <template v-for="item in list">
                                <li v-if="isFolder(item)"
                                    v-on:click.stop.prevent="navigateFolder(item)"
                                    :class="isSelected(item.path) ? 'selected' : ''">
                                    <template v-if="!isBrowserTypeImage">
                                        <input name="selectedItem" type="radio" class="with-gap" :checked="isSelected(item.path)" />
                                        <label v-on:click.stop.prevent="selectItem(item)"></label>
                                    </template>
                                    <i class="material-icons">{{getFolderIcon()}}</i>
                                    <span>{{item.name}}</span>
                                </li>
                                <li v-if="isFile(item) && isFileAllowed()"
                                    v-on:click.stop.prevent="selectItem(item)"
                                    :class="isSelected(item.path) ? 'selected' : ''">
                                    <template v-if="isSelectable(item)">
                                        <input name="selectedItem" type="radio" class="with-gap" :checked="isSelected(item.path)" />
                                        <label></label>
                                    </template>
                                    <i class="material-icons">image</i>
                                    <span>{{item.name}}</span>
                                </li>
                            </template>
                        </ul>
                        <p v-else class="flow-text">{{getEmptyText()}}</p>
                    </template>
                    <template v-if="tab === 'cards' && !search">
                        <nav class="modal-content-nav clearfix">
                            <div class="modal-content-section">
                                <div class="current-folder">
                                    <template v-if="!isRoot">
                                        <a
                                            href="#!"
                                            v-on:click.stop.prevent="selectParent">
                                            <i class="material-icons">keyboard_arrow_left</i>
                                        </a>
                                        {{currentPath}} ({{list.length}})
                                    </template>
                                    <template v-else>
                                        <input
                                            v-if="!isBrowserTypeImage"
                                            type="radio"
                                            class="with-gap"
                                            :checked="isSelected(currentPath)" />
                                        <label v-on:click.stop.prevent="selectItem(nodes)">{{currentPath}} ({{list.length}})</label>
                                    </template>
                                </div>
                            </div>
                            <div class="modal-content-section">
                                <ul class="cards-toolbar sort-nav">
                                    <li>
                                        <span class="cards-toolbar-title">Sort</span>
                                    </li>
                                    <li>
                                        <input
                                            name="assetbrowser_sort_cards"
                                            type="radio"
                                            class="with-gap"
                                            id="assetbrowser_sort_cards_name"
                                            :checked="sortBy === 'name'"/>
                                        <label v-on:click="onSort('name')" for="assetbrowser_sort_cards_name">name</label>
                                    </li>
                                    <li>
                                        <input
                                            name="assetbrowser_sort_cards"
                                            type="radio"
                                            class="with-gap"
                                            id="assetbrowser_sort_cards_type"
                                            :checked="sortBy === 'resourceType'"/>
                                        <label v-on:click="onSort('resourceType')" for="assetbrowser_sort_cards_type">type</label>
                                    </li>
                                    <li>
                                        <input
                                            name="assetbrowser_sort_cards"
                                            type="radio"
                                            class="with-gap"
                                            id="assetbrowser_sort_cards_date"
                                            :checked="sortBy === 'created'"/>
                                        <label v-on:click="onSort('created')" for="assetbrowser_sort_cards_date">date</label>
                                    </li>
                                </ul>
                                <ul class="cards-toolbar filter-nav">
                                    <li>
                                        <span class="cards-toolbar-title">filter</span>
                                    </li>
                                    <li>
                                        <input
                                            name="assetbrowser_filter_cards"
                                            type="radio"
                                            class="with-gap"
                                            id="assetbrowser_filter_cards_all"
                                            :checked="filterBy === '*'"/>
                                        <label v-on:click="onFilter('*')" for="assetbrowser_filter_cards_all">all</label>
                                    </li>
                                    <li>
                                        <input
                                            name="assetbrowser_filter_cards"
                                            type="radio"
                                            class="with-gap"
                                            id="assetbrowser_filter_cards_files"
                                            :checked="filterBy === 'files'"/>
                                        <label v-on:click="onFilter('files')" for="assetbrowser_filter_cards_files">files</label>
                                    </li>
                                    <li>
                                        <input
                                            name="assetbrowser_filter_cards"
                                            type="radio"
                                            class="with-gap"
                                            id="assetbrowser_filter_cards_folders"
                                            :checked="filterBy === 'folders'"/>
                                        <label v-on:click="onFilter('folders')" for="assetbrowser_filter_cards_folders">folders</label>
                                    </li>
                                </ul>
                            </div>
                        </nav>
                        <template v-if="list.length > 0">
                            <p class="range-field">
                                <input
                                    type="range"
                                    min="120"
                                    max="400"
                                    v-model="cardSize"/>
                            </p>
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
                                        v-on:click.stop.prevent="navigateFolder(item)">
                                            <div v-if="isSelectable(item)" class="item-select">
                                                <input name="selectedItem" type="radio" class="with-gap" :checked="isSelected(item.path)" />
                                                <label v-on:click.stop.prevent="selectItem(item)"></label>
                                            </div>
                                            <div class="item-content">
                                                <i
                                                    class="material-icons"
                                                    :style="`font-size: ${cardIconSize(cardSize)}px`">{{getFolderIcon()}}</i>
                                                <br/>
                                                <span class="truncate">{{item.name}}</span>
                                            </div>
                                    </div>
                                    <template v-if="isFile(item) && isFileAllowed()">
                                        <img
                                            v-if="isImage(item)"
                                            :class="isSelected(item.path) ? 'item-image selected' : 'item-image'"
                                            v-bind:style="`width: ${cardSize}px`"
                                            v-bind:src="item.path"
                                            v-on:click.stop.prevent="selectItem(item)"/>
                                        <div
                                            v-else
                                            :class="isSelected(item.path) ? 'item-file selected' : 'item-file'"
                                            :title="item.name"
                                            v-bind:style="`width: ${cardSize}px; height: ${cardSize}px`"
                                            v-on:click.stop.prevent="selectItem(item)">
                                            <div class="item-content">
                                                <i
                                                    class="material-icons"
                                                    :style="`font-size: ${cardIconSize(cardSize)}px`">{{getFileIcon()}}</i>
                                                <br/>
                                                <span class="truncate">{{item.name}}</span>
                                            </div>
                                        </div>

                                    </template>
                                </div>
                            </isotope>
                        </template>
                        <p v-else class="flow-text">{{getEmptyText()}}</p>
                    </template>
                    <template v-if="withLinkTab && tab === 'link' && !search">
                        <div class="form-group">
                            <label for="pathBrowserLink">URL</label>
                            <input
                                id="pathBrowserLink"
                                type="url"
                                placeholder="https://"
                                :value="selectedPath"
                                @input="selectLink" />
                        </div>
                        <div class="form-group" v-if="altText !== undefined">
                            <label for="altText">Image Alternate Text</label>
                            <input
                                id="altText"
                                type="text"
                                placeholder="Alt Text"
                                :value="altText"
                                @input="setAltText" />
                        </div>
                        <div class="form-group" v-if="linkTitle !== undefined">
                            <label for="linkTitle">Link Title</label>
                            <input
                                id="linkTitle"
                                type="text"
                                placeholder="Link Title"
                                :value="linkTitle"
                                @input="setLinkTitle" />
                        </div>
                        <div class="pathbrowser-newwindow" v-if="newWindow !== undefined"
                            @click="toggleNewWindow"
                            @keyup.space="toggleNewWindow">
                            <input type="checkbox" id="newWindow" :checked="newWindow"/>
                            <label for="newWindow">Open in new window?</label>
                        </div>
                    </template>
                </div>
                <div class="col-preview">
                    <template v-if="preview">
                        <div v-if="isFolder(preview)" class="preview-folder">
                            <admin-components-iconopenfolder></admin-components-iconopenfolder>
                        </div>
                        <template v-if="isFile(preview)">
                            <img v-if="true" class="preview-image" v-bind:src="preview.path" />
                            <i v-else class="material-icons preview-file">insert_file_drive</i>
                        </template>
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
                        <span>{{ $i18n('noAssetSelected') }}</span>
                        <i class="material-icons">info</i>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="pathbrowser-footer-details">
                    <span class="pathbrowser-selected-path">{{selectedPath}}</span>
                </div>
                    <div class="pathbrowser-buttons">
                        <button v-on:click="onCancel"
                                class="modal-action waves-effect waves-light btn-flat">
                                cancel
                        </button>
                        <button v-on:click="onSelect"
                                class="modal-action waves-effect waves-light btn-flat">
                                select
                        </button>
                </div>
            </div>
        </div>
        </div>
    </div>
</transition>
</template>

<script>
    import {PathBrowser} from '../../../../../../js/constants';

    export default {
        props: [
            'isOpen',
            'header',
            'browserRoot',
            'browserType',
            'currentPath',
            'selectedPath',
            'withLinkTab',
            'newWindow',
            'toggleNewWindow',
            'setCurrentPath',
            'setSelectedPath',
            'linkTitle',
            'setLinkTitle',
            'altText',
            'setAltText',
            'onCancel',
            'onSelect',
        ],
        watch: {
            cardSize: function (newCardSize) {
                this.updateIsotopeLayout('masonry')
            }
        },
        mounted(){
            // set initial tab
            if(this.withLinkTab && this.selectedPath && this.selectedPath.match(/^(https?:)?\/\//)){
                this.tab = 'link'
            } else {
                this.tab = 'browse'
            }

            this.$on('setSelectedPath', () => {
                console.log('setSelectedPath')
            })
            this.$on('setCurrentPath', () => {
                console.log('setCurrentPath')
            })
        },
        data: function() {
            return {
                tab: null,
                cardSize: 120,
                search: '',
                previewType: this.selectedPath ? 'selected' : 'current',
                sortBy: '',
                filterBy: '*'
            }
        },
        computed: {
            isRoot(){
                return this.currentPath === this.browserRoot
            },
            preview(){
                if(this.previewType === 'selected'){
                    const view = $perAdminApp.getView()
                    return $perAdminApp.findNodeFromPath(view.admin.pathBrowser, this.selectedPath)
                } else {
                    return this.nodes
                }
            },
            nodes() {
                let view = $perAdminApp.getView()
                let nodes = view.admin.pathBrowser
                if(nodes && this.currentPath) {
                  return $perAdminApp.findNodeFromPath(nodes, this.currentPath)
                }
                return {}
            },
            list(){
                if(this.nodes.children){
                    return this.nodes.children
                }
                return []
            },
            tabIndicatorPosition(){
                let position
                switch(this.tab) {
                    case ('browse'):
                        position = 0
                        break
                    case ('cards'):
                        position = 72
                        break
                    case ('link'):
                        position = 144
                        break
                    default:
                        position = 0
                        break
                }
                return position
            },
            searchTabOffset(){
                if(this.withLinkTab){
                    return 216
                } else {
                    return 144
                }
            },
            isBrowserTypeImage() {
              return this.isType(PathBrowser.Type.IMAGE)
            }
        },
        methods: {
            onPrevent(){
                return false
            },
            isImage(item){
                return ['image/png','image/jpeg','image/jpg','image/gif','timage/tiff', 'image/svg+xml'].indexOf(item.mimeType) >= 0
            },
            isImageExtension(item) {
                return item.path.match(/.(jpg|jpeg|png|gif|svg)$/i)
            },
            getFileIcon(){
                return 'insert_drive_file'
            },
            getFolderIcon(){
                return this.isType(PathBrowser.Type.ASSET) ? 'folder_open' : 'description'
            },
            getEmptyText(){
                return this.isType(PathBrowser.Type.ASSET) ? 'Folder is empty' : 'No child pages'
            },
            cardIconSize: function(cardSize){
                return Math.floor(cardSize/3)
            },
            getImagesLoadedCbs: function() {
              return {
                progress: (instance, img ) => {
                  this.updateIsotopeLayout('masonry')
                },
                done: (instance) => {
                  this.updateIsotopeLayout('masonry')
                }
              }
            },
            updateIsotopeLayout: function(layout){
                this.$nextTick(function () {
                    this.$refs.isotope.layout(layout)
                })
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
                        },
                        resourceType: function(itemElem){
                            return itemElem.resourceType.toLowerCase()
                        }
                    },
                    getFilterData:{
                        folders: itemElem => this.isFolder(itemElem),
                        files: itemElem => this.isFile(itemElem)
                    }
                }
            },
            onSort(sortType){
                this.sortBy = sortType
                this.$refs.isotope.sort(sortType)
            },
            onFilter(filterType){
                this.filterBy = filterType
                this.$refs.isotope.filter(filterType)
            },
            select(name) {
                this.tab = name
            },
            searchFilter(item) {
                if(this.search.length === 0) return true
                return (item.name.indexOf(this.search) >= 0)
            },
            selectParent() {
                let parentFolder = this.currentPath.split('/')
                parentFolder.pop()
                let newPath = parentFolder.join('/')
                /* TODO: pass in obj with name and type, not just path */
                this.navigateFolder({ path: newPath})
            },
            display(item) {
                return item.name !== 'jcr:content'
            },
            isFile(item) {
                return ['per:Asset','nt:file'].indexOf(item.resourceType) >= 0
            },
            isFileAllowed(){
                return this.browserType !== PathBrowser.Type.PAGE
            },
            isFolder(item) {
                return ['per:Page','nt:folder', 'sling:Folder', 'sling:OrderedFolder'].indexOf(item.resourceType) >= 0
            },
            isSelected(path) {
                return this.selectedPath === path
            },
            navigateFolder(item) {
                $perAdminApp.getApi().populateNodesForBrowser(item.path, 'pathBrowser')
                    .then( () => {
                        this.previewType = 'current'
                        this.setCurrentPath(item.path)
                        if(this.tab === 'cards' && this.list.length > 0) this.updateIsotopeLayout('masonry')
                        this.selectItem(item)
                    })
            },
            selectItem(item) {
                if (this.isSelectable(item)) {
                    this.previewType = 'selected'
                    this.setSelectedPath(item.path)
                }
            },
            selectLink(ev){
                // TODO: add link preview
                this.previewType = 'link'
                // TODO: allow target="_blank" or target="_self"
                this.setSelectedPath(ev.target.value)
            },
            isType(browserType) {
                return this.browserType === browserType
            },
            isSelectable(item) {
                if (!this.isBrowserTypeImage) {
                  return true
                } else if (this.isImage(item) || this.isImageExtension(item)) {
                  return true
                }
                return false
            }
        }
    }
</script>
