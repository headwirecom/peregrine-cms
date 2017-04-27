<template>
<div class="container">
    <template v-for="segment in pathSegments">
        <admin-components-action v-bind:model="{ target: segment.path, title: segment.name, command: 'selectPath', classes: 'btn waves-effect waves-light'}"></admin-components-action>
    </template>
    <div v-if="pt">
    <ul v-if="pt" class="collection">
        <a class="collection-item" v-for="child in pt.children" v-if="checkIfAllowed(child.resourceType)">
    <admin-components-action v-bind:model="{ target: child.path, title: child.name, command: 'selectPath' }"></admin-components-action>
    &nbsp;
    <a traget="viewer" v-bind:href="viewUrl(child.path)" class="secondary-content"><i class="material-icons">send</i></a>
    &nbsp;
    <admin-components-action v-bind:model="{ target: child.path, command: 'editPage', classes: 'secondary-content'}">
        <i class="material-icons">edit</i>
    </admin-components-action>
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
        props: ['model']
        ,
        data: function() {
            var dataFrom    = this.model.dataFrom

            var segments = dataFrom.split('/').slice(1)

            var node = this.$root.$data
            for(var i = 0; i < segments.length; i++) {
                var next = node[segments[i]]
                if(!next) {
                    next = this.$root.$set(node, segments[i], {})
                }
                node = next
            }

            return { path: node }
        },
        computed: {
            pt: function() {
                console.log(this.$data.path.value)
                return perHelperFindNodeFromPath(this.$root.$data.pages, this.$data.path.value)
            },
            parentPath: function() {
                var segments = this.$data.path.value.toString().split('/')
                var joined = segments.slice(0, segments.length -1).join('/')
                return joined
            },
            pathSegments: function() {
                var segments = this.$data.path.value.toString().split('/')
                var ret = []
                for(var i = 1; i < segments.length; i++) {
                    ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } )
                }
                return ret;
            }
        },
        methods: {
            viewUrl: function(path) {
                var segments = path.split('/')
                var last = segments.pop()
                if(last.indexOf('.') >= 0) {
                    return path
                }
                return path + '.html'
            },
            checkIfAllowed: function(resourceType) {
                return ['nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page'].indexOf(resourceType) >= 0
            },
            selectPath: function(me, target) {
                perHelperModelAction('selectToolsPagesPath', { selected: target, path: me.model.dataFrom })
            },
            viewPage: function(me, target) {
                alert(target)
            },
            addPage: function(me, target) {
                var pageName = prompt('add page at '+me.pt.path)
                perHelperModelAction('createPage', { parent: me.pt.path, name: pageName })
            },
            editPage: function(me, target) {
                perHelperModelAction('editPage', target )
            }
        }

    }
</script>
