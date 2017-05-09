<template>
<div class="explorer container">
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
    <div style="display: flex">
    <div v-if="pt" class="explorer-main">
        <ul v-if="pt" class="collection">
            <a 
                class ="collection-item" 
                v-for ="child in pt.children" 
                v-if  ="checkIfAllowed(child.resourceType)">
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
                            v-bind:href ="viewUrl(child)">
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
            </a>
        </ul>

    </div>
    <div v-if="hasEdit" class="explorer-preview">
        <component v-bind:is="model.children[1].component" v-bind:model="model.children[1]"></component>
    </div>
    </div>
    <template v-for="child in model.children[0].children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>
</div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
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
                return this.model.children[1]
            }
        },
        methods: {
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
                if(nodeType === 'sling:Folder') return 'folder'
                if(nodeType === 'sling:OrderedFolder') return 'folder'
                return 'unknown'
            },
            checkIfAllowed: function(resourceType) {
                return ['nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page', 'sling:OrderedFolder', 'per:Object'].indexOf(resourceType) >= 0
            },
            selectPath: function(me, target) {
                let resourceType = target.resourceType
                console.log(resourceType)
                if(resourceType) {
                    if(resourceType === 'per:Object') {
                        me.selectedObject = target.path
                        $perAdminApp.stateAction('selectObject', { selected: target.path, path: me.model.dataFrom })
                        return
                    }
                    if(resourceType === 'nt:file') {
                        me.selectedObject = target.path
                        $perAdminApp.stateAction('selectAsset', { selected: target.path })
                        return
                    }
                }
                if(me.selectedObject) {
                    $perAdminApp.stateAction('unselectObject', { })
                }
                $perAdminApp.stateAction('selectToolsNodesPath', { selected: target.path, path: me.model.dataFrom })
            },
            viewPage: function(me, target) {
                alert(target)
            },
            addPage: function(me, target) {
                var pageName = prompt('add page at '+me.pt.path)
                $perAdminApp.stateAction('createPage', { parent: me.pt.path, name: pageName, template: '/content/templates/example' })
            },
            addFolder: function(me, target) {
                var folderName = prompt('add folder at '+me.pt.path)
                $perAdminApp.stateAction('createFolder', { parent: me.pt.path, name: folderName })
            },
            addTemplate: function(me, target) {
                var templateName = prompt('add template at '+me.pt.path)
                $perAdminApp.stateAction('createTemplate', { parent: me.pt.path, name: templateName })
            },
            addObject: function(me, target) {
                var objectName = prompt('add object at '+me.pt.path)
                $perAdminApp.stateAction('createObject', { parent: me.pt.path, name: objectName, template: 'example/objects/sample' })
            },
            deletePage: function(me, target) {
                $perAdminApp.stateAction('deletePage', target)
            },
            editPage: function(me, target) {
                $perAdminApp.stateAction('editPage', target )
            }
        }

    }
</script>

<style>
    .explorer-main {
        flex: 6 1 60%;
    }

    .explorer-preview {
        flex: 4 1 40%;
    }

</style>
