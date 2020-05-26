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
                            tooltipTitle: $i18n('backToParentDir')
                        }"><i class="material-icons">folder_open</i> ..
                    </admin-components-action>
                </li>
                <li
                    v-for ="child in children"
                    v-bind:key="child.path"
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
                                tooltipTitle: `${$i18n('select')} '${child.title || child.name}'`
                            }">
                        <i class="material-icons">folder</i>
                    </admin-components-action>

                    <admin-components-action v-if="editable(child)"
                        v-bind:model="{
                            target: child.path,
                            command: 'editPage',
                            dblClickTarget: child,
                            dblClickCommand: 'selectPath',
                            tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                        }"><i class="material-icons">{{nodeTypeToIcon(child.resourceType)}}</i> {{child.title ? child.title : child.name}}
                    </admin-components-action>

                    <admin-components-action v-if="!editable(child)"
                        v-bind:model="{
                            target: child,
                            command: 'selectPath',
                            tooltipTitle: `${$i18n('select')} '${child.title || child.name}'`
                        }"><i class="material-icons">{{nodeTypeToIcon(child.resourceType)}}</i> {{child.title ? child.title : child.name}}
                    </admin-components-action>

                    <admin-components-extensions v-bind:model="{id: 'admin.components.explorer', item: child}"></admin-components-extensions>

                    <div class="secondary-content">
                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'editPage',
                                tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                            }">
                            <admin-components-iconeditpage></admin-components-iconeditpage>
                        </admin-components-action>

                        <admin-components-action v-if="composumEditable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'editFile',
                                tooltipTitle: `${$i18n('editFile')} '${child.title || child.name}'`
                            }">
                            <admin-components-iconeditpage></admin-components-iconeditpage>
                        </admin-components-action>

                        <admin-components-action v-if="replicable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'replicate',
                                tooltipTitle: `${$i18n('replicate')} '${child.title || child.name}'`
                            }">
                            <i class="material-icons" v-bind:class="replicatedClass(child)">public</i>
                        </admin-components-action>

                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'showInfo',
                                tooltipTitle: `'${child.title || child.name}' ${$i18n('info')}`
                            }">
                            <i class="material-icons">info</i>
                        </admin-components-action>

                        <span v-if="viewable(child)">
                            <a
                                target      ="viewer"
                                v-bind:href ="viewUrl(child)"
                                v-on:click.stop  =""
                                v-bind:title="`${$i18n('view')} '${child.title || child.name}' ${$i18n('inNewTab')}`"
                                >
                                <i class="material-icons">visibility</i>
                            </a>
                        </span>

                        <admin-components-action
                            v-bind:model="{
                                target: child,
                                command: 'deleteTenantOrPage',
                                tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`
                            }">
                            <i class="material-icons">delete</i>
                        </admin-components-action>
                    </div>
                </li>
                <li class="collection-item" v-if="isPages(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addPage',
                            tooltipTitle: `${$i18n('add page')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add page')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isObjects(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addObject',
                            tooltipTitle: `${$i18n('add object')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add object')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isTemplates(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addTemplate',
                            tooltipTitle: `${$i18n('add template')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add template')}}
                    </admin-components-action>
                </li>
            </ul>
            <div v-if="children && children.length == 0" class="empty-explorer">
                <div v-if="path.includes('assets')">
                    {{ $i18n('emptyExplorerHintAssets') }}.
                </div>
                <div v-else>
                    {{ $i18n('emptyExplorerHint') }}...
                </div>
            </div>
        </div>
        <div v-else class="col s12 m8 explorer-main explorer-empty">
            <div>
                <span>{{ $i18n(`nothing to show`) }}</span>
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

    import {set} from '../../../../../../js/utils';

    export default {
        props: ['model'],

        data() {
            return {
                isDraggingFile: false,
                isDraggingUiEl: false,
                isFileUploadVisible: false,
                uploadProgress: 0
            }
        },

        computed: {
            showNavigateToParent() {
                return this.path.split('/').length > 4
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
                var joined = segments.slice(0, segments.length - 1).join('/')
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
            getTenant() {
              return $perAdminApp.getView().state.tenant || {name: 'example'}
            },

            isAssets(path) {
                return path.startsWith(`/content/${this.getTenant().name}/assets`)
            },

            isPages(path) {
                return path.startsWith(`/content/${this.getTenant().name}/pages`)
            },

            isObjects(path) {
                return path.startsWith(`/content/${this.getTenant().name}/objects`)
            },

            isTemplates(path) {

                return path.startsWith(`/content/${this.getTenant().name}/templates`)
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

            replicable(item) {
                return true
            },

            onDragRowStart(item, ev) {
                ev.srcElement.classList.add("dragging");
                ev.dataTransfer.setData('text', item.path)
                if(this.isDraggingFile) { this.isDraggingFile = false }
                this.isDraggingUiEl = true
            },

            onDragRow(ev) {
                ev.srcElement.classList.remove("dragging");
            },

            onDragRowEnd(item, ev) {
                this.isDraggingUiEl = false
            },

            onDragOverRow(ev) {
                if(this.isDraggingUiEl) {
                    const center = ev.target.offsetHeight / 2 ;
                    this.dropType = ev.offsetY > center ? 'after' : 'before';
                    ev.target.classList.toggle('drop-after', ev.offsetY > center );
                    ev.target.classList.toggle('drop-before', ev.offsetY < center );
                }
            },

            onDragEnterRow(ev) {
            },

            onDragLeaveRow(ev) {
                if(this.isDraggingUiEl) {
                    ev.target.classList.remove('drop-after','drop-before')
                }
            },

            onDropRow(item, ev, type) {
                if(this.isDraggingUiEl) {
                    ev.target.classList.remove('drop-after','drop-before')
                    const dataFrom = this.model.dataFrom
                    const path = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                    let action
                    switch(true) {
                        case (this.isPages(path)):
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

            onDragOverExplorer(ev) {
            },

            onDragEnterExplorer(ev) {
                if(!this.isAssets(this.path)) return
                if(this.isDraggingUiEl) return
                this.isDraggingFile = true
                this.isFileUploadVisible = true
            },

            onDragLeaveExplorer(ev) {
                /* hide upload overlay */
                /* TODO: fix upload unexpectedly closing
                if(this.isDraggingFile) {
                    this.isDraggingFile = false
                }
                */
            },

            onDropExplorer(ev) {
                if(!this.isAssets(this.path)) return
                if(this.isDraggingUiEl) return
                if(this.isDraggingFile) {
                    this.uploadFile(ev.dataTransfer.files)
                    this.isDraggingFile = false
                }
            },

            uploadFile(files) {
              $perAdminApp.stateAction('uploadFiles', {
                path: $perAdminApp.getView().state.tools.assets,
                files: files,
                cb: this.setUploadProgress
              })
            },

            setUploadProgress(percentCompleted) {
              this.uploadProgress = percentCompleted
            },

            onDoneFileUpload() {
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

            composumEditable: function(child) {
                return ['nt:file'].indexOf(child.resourceType) >= 0
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
                if(nodeType === 'per:Page')             return 'description'
                if(nodeType === 'per:Object')           return 'layers'
                if(nodeType === 'nt:file')              return 'insert_drive_file'
                if(nodeType === 'per:Asset')            return 'image'
                if(nodeType === 'sling:Folder')         return 'folder'
                if(nodeType === 'sling:OrderedFolder')  return 'folder'
                return 'unknown'
            },

            checkIfAllowed: function(resourceType) {
                return ['per:Asset', 'nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page', 'sling:OrderedFolder', 'per:Object'].indexOf(resourceType) >= 0
            },

            showInfo: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                if(target.startsWith(`/content/${tenant.name}/objects`)) {
                    const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, target)
                    $perAdminApp.stateAction('selectObject', { selected: node.path, path: me.model.dataFrom })
                } else if (target.startsWith(`/content/${tenant.name}/templates`)) {
                    $perAdminApp.stateAction('showTemplateInfo', { selected: target })
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
                const payload = { selected: target.path, path: me.model.dataFrom }
                $perAdminApp.stateAction('selectToolsNodesPath', payload).then( () => {
                    $('div.brand-logo span').last().click() //TODO: quick and dirty solution!!!!
                })
            },

            selectPathInNav: function(me, target) {
                this.selectPath(me, target)
            },

            addSite: function(me, target) {
                $perAdminApp.stateAction('createTenantWizard', '/content')
            },

            addPage: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/pages`)) {
                    $perAdminApp.stateAction('createPageWizard', path)
                }
            },

            addTemplate: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/templates`)) {
                    $perAdminApp.stateAction('createTemplateWizard', path)
                }
            },

            addObject: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/objects`)) {
                    $perAdminApp.stateAction('createObjectWizard', { path: path, target: target })
                }
            },

            addFolder: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/assets`)) {
                    $perAdminApp.stateAction('createAssetFolderWizard', path)
                } else if(path.startsWith(`/content/${tenant.name}/objects`)) {
                    $perAdminApp.stateAction('createObjectFolderWizard', path)
                }
            },

            sourceImage: function(me, target) {
                $perAdminApp.stateAction('sourceImageWizard', me.pt.path)
            },

            deleteTenantOrPage: function(me, target) {
                if(me.path === '/content') {
                    me.deleteTenant(me, target)
                } else {
                    me.deletePage(me, target)
                }
            },

            deletePage: function(me, target) {
                $perAdminApp.askUser('Delete Page', me.$i18n('Are you sure you want to delete this node and all its children?'), {
                    yes() {
                        const resourceType = target.resourceType
                        if(resourceType === 'per:Object') {
                            $perAdminApp.stateAction('deleteObject', target.path)
                        } else if(resourceType === 'per:Asset') {
                            $perAdminApp.stateAction('deleteAsset', target.path)
                        } else if(resourceType === 'sling:OrderedFolder') {
                            $perAdminApp.stateAction('deleteFolder', target.path)
                        } else if(resourceType === 'per:Page') {
                            $perAdminApp.stateAction('deletePage', target.path)
                        } else if(resourceType === 'nt:file') {
                            $perAdminApp.stateAction('deleteFile', target.path)
                        }else {
                            $perAdminApp.stateAction('deleteFolder', target.path)
                        }
                    }
                })
            },

            deleteTenant: function(me, target) {
                $perAdminApp.askUser('Delete Site', me.$i18n('Are you sure you want to delete this site, its children, and generated content and components?'), {
                    yes() {
                        $perAdminApp.stateAction('deleteTenant', target)
                    }
                })
            },

            editPage: function(me, target) {
                const view = $perAdminApp.getView()
                const tenant = view.state.tenant
                const path = me.pt.path

                if(target.startsWith(`/content/${tenant.name}/pages`)) {
                    set(view, '/state/tools/page', target)
                } else if(target.startsWith(`/content/${tenant.name}/templates`)) {
                    set(view, '/state/tools/template', target)
                }

                if(path.startsWith(`/content/${tenant.name}/objects`)) {
                    const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, target)
                    me.selectedObject = path
                    $perAdminApp.stateAction('editObject', { selected: node.path, path: me.model.dataFrom })
                } else if (path.startsWith(`/content/${tenant.name}/templates`)) {
                    $perAdminApp.stateAction('editTemplate', target )
                } else {
                    $perAdminApp.stateAction('editPage', target )
                }
            },

            editFile: function(me, target) {
                window.open(`/bin/cpm/edit/code.html${target}`, 'composum')
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

    .explorer-empty {
        display: flex;
        justify-content: center;
        align-items: center;
    }
</style>
