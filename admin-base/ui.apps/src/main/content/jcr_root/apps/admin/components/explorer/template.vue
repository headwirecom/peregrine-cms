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

<div class="explorer"
    v-on:dragover.prevent  ="onDragOverExplorer"
    v-on:dragenter.prevent ="onDragEnterExplorer"
    v-on:dragleave.prevent ="onDragLeaveExplorer"
    v-on:drop.prevent      ="onDropExplorer">

    <div class="explorer-layout">
    <div class="row">
        <div v-if="pt" class="col s12 m8 explorer-main">
            <ul class="collection">
                <li v-if="showNavigateToParent"
                    v-on:click.stop.prevent="selectParent()"
                    class="collection-item">
                    <admin-components-action
                            v-bind:model="{
                            target: null,
                            command: 'selectParent',
                            tooltipTitle: $i18n('select parent')
                        }"><i class="material-icons">folder_open</i> ..
                    </admin-components-action>
                </li>
                <li
                    v-for ="(child,i) in children"
                    v-bind:key="i"
                    v-bind:class="`collection-item ${isSelected(child) ? 'explorer-item-selected' : ''}`"
                    draggable ="true"
                    v-on:dragstart ="onDragRowStart(child,$event)"
                    v-on:drag      ="onDragRow"
                    v-on:dragend   ="onDragRowEnd(child,$event)"
                    v-on:dragenter.stop.prevent ="onDragEnterRow"
                    v-on:dragover.stop.prevent  ="onDragOverRow"
                    v-on:dragleave.stop.prevent ="onDragLeaveRow"
                    v-on:drop.prevent      ="onDropRow(child, $event)">

                    <admin-components-draghandle/>

                    <admin-components-action v-if="editable(child)"
                                             v-bind:model="{
                                target: child,
                                command: 'selectPath',
                                tooltipTitle: `select '${child.title || child.name}'`
                            }">
                        </span><i class="material-icons">folder</i>
                    </admin-components-action>

                    <admin-components-action v-if="editable(child)"
                        v-bind:model="{
                            target: child.path,
                            command: 'editPage',
                            dblClickTarget: child,
                            dblClickCommand: 'selectPath',
                            tooltipTitle: `edit '${child.title || child.name}'`
                        }"><i class="material-icons">{{nodeTypeToIcon(child.resourceType)}}</i> {{child.title ? child.title : child.name}}
                    </admin-components-action>

                    <admin-components-action v-if="!editable(child)"
                        v-bind:model="{
                            target: child,
                            command: 'selectPath',
                            tooltipTitle: `select '${child.title || child.name}'`
                        }"><i class="material-icons">{{nodeTypeToIcon(child.resourceType)}}</i> {{child.title ? child.title : child.name}}
                    </admin-components-action>

                    <admin-components-extensions v-bind:model="{id: 'admin.components.explorer', item: child}"></admin-components-extensions>

                    <div class="secondary-content">
                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'editPage',
                                tooltipTitle: `edit '${child.title || child.name}'`
                            }">
                            <admin-components-iconeditpage></admin-components-iconeditpage>
                        </admin-components-action>

                        <admin-components-action v-if="replicatable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'replicate',
                                tooltipTitle: `replicate '${child.title || child.name}'`
                            }">
                            <i class="material-icons" v-bind:class="replicatedClass(child)">public</i>
                        </admin-components-action>

                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'showInfo',
                                tooltipTitle: `'${child.title || child.name}' info`
                            }">
                            <i class="material-icons">info</i>
                        </admin-components-action>

                        <span v-if="viewable(child)">
                            <a
                                target      ="viewer"
                                v-bind:href ="viewUrl(child)"
                                v-on:click.stop  =""
                                v-bind:title="`view '${child.title || child.name}' in new tab`"
                                >
                                <i class="material-icons">visibility</i>
                            </a>
                        </span>

                        <admin-components-action
                            v-bind:model="{
                                target: child,
                                command: 'deletePage',
                                tooltipTitle: `delete '${child.title || child.name}'`
                            }">
                            <i class="material-icons">delete</i>
                        </admin-components-action>
                    </div>
                </li>
            </ul>
            <div v-if="children && children.length == 0" class="empty-explorer">
                <div v-if="path.startsWith('/content/assets')">
                    This folder is empty, use the navigation bar to add an asset or drag and drop an asset
                    from the file system onto the browser.
                </div>
                <div v-else>
                    This folder is empty, use the navigation bar to add content...
                </div>
            </div>

        </div>
        <admin-components-explorerpreview v-if="hasEdit">
            <component v-bind:is="model.children[0].component" v-bind:model="model.children[0]"></component>
        </admin-components-explorerpreview>
    </div>
    </div>

    <div v-if="isFileUploadVisible" class="file-upload">
        <div class="file-upload-inner">
            <i class="material-icons">file_download</i>
            <span class="file-upload-text">Drag &amp; Drop files anywhere</span>
            <div class="progress-bar">
                <div class="progress-bar-value" v-bind:style="`width: ${uploadProgress}%`"></div>
            </div>
            <div class="progress-text">{{uploadProgress}}%</div>
            <div class="file-upload-action">
                <button
                    type="button"
                    class="btn"
                    v-on:click="onDoneFileUpload">ok</button>
            </div>
            <!-- <progress class="file-upload-progress" v-bind:value="uploadProgress" max="100"></progress> -->
        </div>
    </div>
    <!--
    <template v-for="child in model.children[0].children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>
    -->
</div>
</template>

<script>
    export default {
        props: ['model'],
        data(){
            return {
                isDraggingFile: false,
                isDraggingUiEl: false,
                isFileUploadVisible: false,
                uploadProgress: 0
            }
        },
        computed: {
            showNavigateToParent() {
                return this.path.split('/').length > 3
            },
            path: function() {
                var dataFrom = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                return node
            },
            pt: function() {
                var node = this.path
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, node)
            },
            children: function() {
                if ( this.pt.children ) {
                    return this.pt.children.filter( child => this.checkIfAllowed(child.resourceType) )
                }
            },
            parentPath: function() {
                var segments = this.$data.path.value.toString().split('/')
                var joined = segments.slice(0, segments.length -1).join('/')
                return joined
            },
            pathSegments: function() {
                var segments = this.path.toString().split('/')
                var ret = []
                for(var i = 1; i < segments.length; i++) {
                    ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } )
                }
                return ret;
            },
            hasEdit: function() {
                return this.model.children && this.model.children[0]
            }
        },
        methods: {
            isAssets(path){
                return path.startsWith('/content/assets')
            },
            isSites(path){
                return path.startsWith('/content/sites')
            },
            isObjects(path){
                return path.startsWith('/content/objects')
            },
            isTemplates(path){
                return path.startsWith('/content/templates')
            },
            selectParent(me, target) {
                var dataFrom = !me ? this.model.dataFrom : me.model.dataFrom
                var path = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                var pathSegments = path.split('/')
                pathSegments.pop()
                path = pathSegments.join('/')
                $perAdminApp.action(!me ? this: me, 'selectPath', { path: path, resourceType: 'sling:OrderedFolder'})
            },
            selectItem(item) {
                $perAdminApp.action(this, 'selectPath', item)
            },

            replicatedClass(item) {
                if(item.ReplicationStatus) {
                    const created = item.created
                    const modified = item.lastModified ? item.lastModified : created
                    const replicated = item.Replicated
                    if(replicated > modified) {
                        return 'item-'+item.ReplicationStatus
                    } else {
                        return 'item-'+item.ReplicationStatus+'-modified'
                    }
                }
                return 'item-replication-unknown'
            },
            replicate(me, path) {
                $perAdminApp.stateAction('replicate', path)
            },
            replicatable(item) {
                return true
            },
            /* row drag events */
            onDragRowStart(item, ev){
                ev.srcElement.classList.add("dragging");
                ev.dataTransfer.setData('text', item.path)
                if(this.isDraggingFile){ this.isDraggingFile = false }
                this.isDraggingUiEl = true
            },

            onDragRow(ev){
                ev.srcElement.classList.remove("dragging");
            },

            onDragRowEnd(item, ev){
                this.isDraggingUiEl = false
            },

            /* row drop zone events */
            onDragOverRow(ev){
                if(this.isDraggingUiEl){
                    const center = ev.target.offsetHeight / 2 ;
                    this.dropType = ev.offsetY > center ? 'after' : 'before';
                    ev.target.classList.toggle('drop-after', ev.offsetY > center );
                    ev.target.classList.toggle('drop-before', ev.offsetY < center );
                }
            },

            onDragEnterRow(ev){
            },

            onDragLeaveRow(ev){
                if(this.isDraggingUiEl){
                    ev.target.classList.remove('drop-after','drop-before')
                }
            },

            onDropRow(item, ev, type) {
                if(this.isDraggingUiEl){
                    ev.target.classList.remove('drop-after','drop-before')
                    /* reorder row logic */
                    const dataFrom = this.model.dataFrom
                    const path = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                    let action
                    switch(true) {
                        case (this.isSites(path)):
                            action = 'movePage'
                            break
                        case (this.isTemplates(path)):
                            action = 'moveTemplate'
                            break
                        case (this.isObjects(path)):
                            action = 'moveObject'
                            break
                        case (this.isAssets(path)):
                            action = 'moveAsset'
                            break
                        default:
                            console.warn('path is not a site, asset, template or object.')
                    }

                    $perAdminApp.stateAction(action, {
                        path: ev.dataTransfer.getData("text"),
                        to: item.path,
                        type: this.dropType
                    })
                }
            },

            /* file drop zone events */
            onDragOverExplorer(ev){

            },

            onDragEnterExplorer(ev){
                if(!this.isAssets(this.path)) return
                if(this.isDraggingUiEl) return
                this.isDraggingFile = true
                this.isFileUploadVisible = true
            },

            onDragLeaveExplorer(ev){
                /* hide upload overlay */
                /* TODO: fix upload unexpectedly closing
                if(this.isDraggingFile){
                    this.isDraggingFile = false
                }
                */
            },

            onDropExplorer(ev){
                if(!this.isAssets(this.path)) return
                if(this.isDraggingUiEl) return
                if(this.isDraggingFile){
                    /* file uploade logic */
                    this.uploadFile(ev.dataTransfer.files)
                    this.isDraggingFile = false
                }
            },

            /* file upload */
            uploadFile(files) {
              $perAdminApp.stateAction('uploadFiles', {
                path: $perAdminApp.getView().state.tools.assets,
                files: files,
                cb: this.setUploadProgress
              })
            },

            setUploadProgress(percentCompleted){
              this.uploadProgress = percentCompleted
            },

            onDoneFileUpload(){
                this.isFileUploadVisible = false
                this.uploadProgress = 0
            },


            isSelected: function(child) {
                if(this.model.selectionFrom && child) {
                    return $perAdminApp.getNodeFromViewOrNull(this.model.selectionFrom) === child.path
                } else if(child.path === $perAdminApp.getNodeFromViewOrNull('/state/tools/page')) {
                    return true
                }
                return false

            },
            hasChildren: function(child) {
                return child && child.childCount && child.childCount > 0;
            },
            editable: function(child) {
                return ['per:Page', 'per:Object'].indexOf(child.resourceType) >= 0
            },
            viewable: function(child) {
                return ['per:Page', 'per:Object', 'nt:file'].indexOf(child.resourceType) >= 0
            },
            viewUrl: function(child) {
                var path = child.path
                var segments = path.split('/')
                var last = segments.pop()
                if(last.indexOf('.') >= 0) {
                    return path
                }
                if(child.resourceType === 'per:Page') {
                    return path + '.html'
                }
                return path + '.json'
            },
            nodeTypeToIcon: function(nodeType) {

                if(nodeType === 'per:Page')     return 'description'
                if(nodeType === 'per:Object')   return 'layers'
                if(nodeType === 'nt:file')      return 'insert_drive_file'
                if(nodeType === 'per:Asset')      return 'image'
                if(nodeType === 'sling:Folder') return 'folder'
                if(nodeType === 'sling:OrderedFolder') return 'folder'
                return 'unknown'
            },
            checkIfAllowed: function(resourceType) {
                return ['per:Asset', 'nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page', 'sling:OrderedFolder', 'per:Object'].indexOf(resourceType) >= 0
            },
            showInfo: function(me, target) {
                if(target.startsWith('/content/objects')) {
                    const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, target)
                    $perAdminApp.stateAction('selectObject', { selected: node.path, path: me.model.dataFrom })
                } else {
                    $perAdminApp.stateAction('showPageInfo', { selected: target })
                }
            },
            selectPath: function(me, target) {
                let resourceType = target.resourceType
                if(resourceType) {
//                    if(resourceType === 'per:Object') {
//                        me.selectedObject = target.path
//                        $perAdminApp.stateAction('selectObject', { selected: target.path, path: me.model.dataFrom })
//                        return
//                    }
                    if(resourceType === 'per:Asset') {
                        $perAdminApp.stateAction('selectAsset', { selected: target.path })
                        return
                    } else if(resourceType === 'nt:file') {
                        return
                    }
                }
                if($perAdminApp.getNodeFromView('/state/tools/object/show')) {
                    $perAdminApp.stateAction('unselectObject', { })
                }
                if($perAdminApp.getNodeFromView('/state/tools/asset/show')) {
                    $perAdminApp.stateAction('unselectAsset', { })
                }
                $perAdminApp.stateAction('selectToolsNodesPath', { selected: target.path, path: me.model.dataFrom })
            },
            selectPathInNav: function(me, target) {
                this.selectPath(me, target)
            },
            viewPage: function(me, target) {
                alert(target)
            },
            addPage: function(me, target) {
                if(me.pt.path === '/content/sites') {
                    $perAdminApp.stateAction('createSiteWizard', me.pt.path)
//                    $perAdminApp.notifyUser('create new site', 'to create a new site root, please visit the documentation on how to start a new site', null)
                } else {
                    $perAdminApp.stateAction('createPageWizard', me.pt.path)
                }
            },
            addFolder: function(me, target) {
                if(me.pt.path.startsWith('/content/assets')) {
                    $perAdminApp.stateAction('createAssetFolderWizard', me.pt.path)
                } else if(me.pt.path.startsWith('/content/objects')) {
                    $perAdminApp.stateAction('createObjectFolderWizard', me.pt.path)
                }
            },
            sourceImage: function(me, target) {
                $perAdminApp.stateAction('sourceImageWizard', me.pt.path )
            },
            addTemplate: function(me, target) {
                if(me.pt.path === '/content/templates') {
                    $perAdminApp.notifyUser('create new site', 'to create a new site root, please visit the documentation on how to start a new site', null)
                } else {
                    $perAdminApp.stateAction('createTemplateWizard', me.pt.path)
                }
            },
            addObject: function(me, target) {
                $perAdminApp.stateAction('createObjectWizard', { path: me.pt.path, target: target })
            },
            deletePage: function(me, target) {
                const really = confirm('Are you sure to delete this node and all its children?')
                if(!really) return
                const resourceType = target.resourceType
                if(resourceType === 'per:Object') {
                    $perAdminApp.stateAction('deleteObject', target.path)
                } else if(resourceType === 'per:Asset') {
                        $perAdminApp.stateAction('deleteAsset', target.path)
                } else if(resourceType === 'sling:OrderedFolder') {
                    $perAdminApp.stateAction('deleteFolder', target.path)
                } else {
                    $perAdminApp.stateAction('deletePage', target.path)
                }
            },
            editPage: function(me, target) {
                const path = me.pt.path
                if(path.startsWith('/content/templates')) {
                    $perAdminApp.stateAction('editTemplate', target )
                } else if(path.startsWith('/content/objects')) {
                    const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, target)
                    me.selectedObject = path
                    $perAdminApp.stateAction('editObject', { selected: node.path, path: me.model.dataFrom })
                } else {
                    $perAdminApp.stateAction('editPage', target )
                }
            }
        }

    }
</script>

<style>
    .item-activated {
        color: green;
    }

    .item-replication-unknown {
    }

    .item-deactivated {
        color: red;
    }

    .item-activated-modified {
        color: purple;
    }

</style>
