<template>
<div class="container">
    <template v-for="segment in pathSegments">
        <admin-components-action v-bind:target="segment.path" v-bind:title="segment.name" v-bind:command="'selectPath'" classes="btn waves-effect waves-light"></admin-components-action>
    </template>
    <div v-if="pt">
    <ul v-if="pt" class="collection">
        <a class="collection-item" v-for="child in pt.children" v-if="child.resourceType === 'per:Page'">
    <admin-components-action v-bind:target="child.path" v-bind:title="child.name" v-bind:command="'selectPath'"></admin-components-action>
    &nbsp;
    <a traget="viewer" v-bind:href="child.path + '.html'" class="secondary-content"><i class="material-icons">send</i></a>
    &nbsp;
    <admin-components-action v-bind:target="child.path" v-bind:command="'editPage'" classes="secondary-content">
        <i class="material-icons">edit</i>
    </admin-components-action>
        </a>
    </ul>
    <admin-components-action v-bind:target="pt.path" v-bind:title="'add page'" v-bind:command="'addPage'"></admin-components-action>
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
            selectPath: function(me, target) {
                perHelperModelAction('selectToolsPagesPath', target)
            },
            viewPage: function(me, target) {
                alert(target)
            },
            addPage: function(me, target) {
                var pageName = prompt('add page')
                perHelperModelAction('createPage', { parent: target, name: pageName })
            },
            editPage: function(me, target) {
                perHelperModelAction('editPage', target )
            }
        }

    }
</script>
