<template>
<div class="explorer container">
    <template v-for="segment in pathSegments">
        <admin-components-action 
            v-bind:model="{ 
                target: segment.path, 
                title: segment.name, 
                command: 'selectPath', 
                classes: 'btn waves-effect waves-light blue-grey darken-3'
            }">
        </admin-components-action>
    </template>
    <div v-if="pt">
        <ul v-if="pt" class="collection">
            <a 
                class ="collection-item" 
                v-for ="child in pt.children" 
                v-if  ="checkIfAllowed(child.resourceType)">
                <admin-components-action 
                    v-bind:model="{ 
                        target: child.path, 
                        command: 'selectPath'
                    }"><i class="material-icons">{{nodeTypeToIcon(child.resourceType)}}</i> {{child.name}}
                </admin-components-action>

                <div class="secondary-content">
                    <admin-components-action v-show="editable(child)"
                        v-bind:model="{ 
                            target: child.path, 
                            command: 'editPage'
                        }">
                        <i class="material-icons">edit</i>
                    </admin-components-action>
                    <span>
                        <admin-components-action
                            v-bind:model="{
                                target: child.path,
                                command: 'deletePage'
                            }">
                            <i class="material-icons">delete</i>
                        </admin-components-action>
                    </span>
                    <span v-show="viewable(child)">
                        <a 
                            target      ="viewer"
                            v-bind:href ="viewUrl(child)">
                            <i class="material-icons">visibility</i>
                        </a>
                    </span>
                </div>
            </a>
        </ul>

        <template v-for="child in model.children">
            <component v-bind:is="child.component" v-bind:model="child"></component>
        </template>
    </div>
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

                if(nodeType === 'per:Page')     return 'restore_page'
                if(nodeType === 'per:Object')   return 'layers'
                if(nodeType === 'nt:file')      return 'restore_page'
                if(nodeType === 'sling:Folder') return 'folder'
                if(nodeType === 'sling:OrderedFolder') return 'folder'
                return 'unknown'
            },
            checkIfAllowed: function(resourceType) {
                return ['nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page', 'sling:OrderedFolder', 'per:Object'].indexOf(resourceType) >= 0
            },
            selectPath: function(me, target) {
                $perAdminApp.stateAction('selectToolsNodesPath', { selected: target, path: me.model.dataFrom })
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
