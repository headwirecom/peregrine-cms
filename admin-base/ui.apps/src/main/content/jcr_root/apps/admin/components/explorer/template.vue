<template>

<div class="explorer" 
    v-on:drag.prevent      ="stopPropagation"
    v-on:dragstart.prevent ="stopPropagation"
    v-on:dragover.prevent  ="setDragState"
    v-on:dragenter.prevent ="setDragState"
    v-on:dragleave.prevent ="unSetDragState"
    v-on:dragend.prevent   ="unSetDragState"
    v-on:drop.prevent      ="onDropFile">
    <!--
    <template v-for="segment in pathSegments">
        <admin-components-action 
            v-bind:model="{ 
                target: { path: segment.path },
                title: segment.name, 
                command: 'selectPath', 
                classes: 'btn waves-effect waves-light blue-grey darken-3'
            }">
        </admin-components-action>
    </template>
    -->
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
                            command: 'selectParent'
                        }"><i class="material-icons">folder</i> ..
                    </admin-components-action>
                </li>
                <li 
                    v-bind:class="`collection-item ${isSelected(child) ? 'explorer-item-selected' : ''}`"
                    v-for ="child in pt.children"
                    v-if  ="checkIfAllowed(child.resourceType)"
                    v-on:click.stop.prevent="selectItem(child)">
                    <admin-components-action
                        v-bind:model="{
                            target: child,
                            command: 'selectPath'
                        }"><i class="material-icons">{{nodeTypeToIcon(child.resourceType)}}</i> {{child.name}}
                    </admin-components-action>

                    <div class="secondary-content">
                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{ 
                                target: child.path, 
                                command: 'editPage'
                            }">
                            <i class="material-icons">edit</i>
                        </admin-components-action>

                        <span v-if="viewable(child)">
                            <a 
                                target      ="viewer"
                                v-bind:href ="viewUrl(child)"
                                v-on:click.stop  =""
                                >
                                <i class="material-icons">visibility</i>
                            </a>
                        </span>

                        <admin-components-action
                            v-bind:model="{
                                target: child.path,
                                command: 'deletePage'
                            }">
                            <i class="material-icons">delete</i>
                        </admin-components-action>
                    </div>
                </li>
            </ul>

        </div>
        <div v-if="hasEdit" class="col s12 m4 explorer-preview">
            <component v-bind:is="model.children[0].component" v-bind:model="model.children[0]"></component>
        </div>
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
                isDragging: false,
                uploadProgress: 0
            }
        },
        computed: {
            showNavigateToParent() {
                return this.path.split('/').length > 3
            },
            path: function() {
                var dataFrom    = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                return node
            },
            pt: function() {
                var node = this.path
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, node)
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
            /* file upload */
            setDragState(ev){
              ev.stopPropagation()
              this.isDragging = true
            },
            unSetDragState(ev){
              ev.stopPropagation()
              this.isDragging = false
            },
            stopPropagation(ev){
              ev.stopPropagation()
            },
            uploadFile(files) {
              $perAdminApp.stateAction('uploadFiles', { 
                path: $perAdminApp.getView().state.tools.assets, 
                files: files,
                cb: this.setUploadProgress
              })    
            },
            setUploadProgress(percentCompleted){
              this.uploadProgress = percentCompleted 
              if(percentCompleted === 100){
                $perAdminApp.notifyUser(
                  'Success', 
                  'File uploaded successfully.', 
                  () => { this.uploadProgress = 0 }
                ) 
              }
            },
            onDropFile(ev){
              console.log('onDropFile')
              ev.stopPropagation()
              this.isDragging = false
              this.uploadFile(ev.dataTransfer.files)
            },
            
            isSelected: function(child) {

                if(this.model.selectionFrom && child) {
                    return $perAdminApp.getNodeFromViewOrNull(this.model.selectionFrom) === child.path
                }
                return false

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

                if(nodeType === 'per:Page')     return 'insert_drive_file'
                if(nodeType === 'per:Object')   return 'layers'
                if(nodeType === 'nt:file')      return 'insert_drive_file'
                if(nodeType === 'per:Asset')      return 'insert_drive_file'
                if(nodeType === 'sling:Folder') return 'folder'
                if(nodeType === 'sling:OrderedFolder') return 'folder'
                return 'unknown'
            },
            checkIfAllowed: function(resourceType) {
                return ['per:Asset', 'nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page', 'sling:OrderedFolder', 'per:Object'].indexOf(resourceType) >= 0
            },
            selectPath: function(me, target) {
                let resourceType = target.resourceType
                if(resourceType) {
                    if(resourceType === 'per:Object') {
                        me.selectedObject = target.path
                        $perAdminApp.stateAction('selectObject', { selected: target.path, path: me.model.dataFrom })
                        return
                    }
                    if(resourceType === 'per:Asset') {
                        me.selectedAsset = target.path
                        $perAdminApp.stateAction('selectAsset', { selected: target.path })
                        return
                    }
                }
                if(me.selectedObject) {
                    $perAdminApp.stateAction('unselectObject', { })
                }
                if(me.selectedAsset) {
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
                    $perAdminApp.notifyUser('create new site', 'to create a new site root, please visit the documentation on how to start a new site', null)
                } else {
                    $perAdminApp.stateAction('createPageWizard', me.pt.path)
                }
            },
            addFolder: function(me, target) {
                var folderName = prompt('add folder at '+me.pt.path)
                if(folderName) {
                    $perAdminApp.stateAction('createFolder', { parent: me.pt.path, name: folderName })
                }
            },
            sourceImage: function(me, target) {
                $perAdminApp.stateAction('sourceImageWizard', me.pt.path )
            },
            addTemplate: function(me, target) {
                $perAdminApp.stateAction('createTemplateWizard', me.pt.path)
            },
            addObject: function(me, target) {
                $perAdminApp.stateAction('createObjectWizard', me.pt.path)
            },
            deletePage: function(me, target) {
                $perAdminApp.stateAction('deletePage', target)
            },
            editPage: function(me, target) {
                if(me.pt.path.startsWith('/content/templates')) {
                    $perAdminApp.stateAction('editTemplate', target )
                } else {
                    $perAdminApp.stateAction('editPage', target )
                }
            }
        }

    }
</script>