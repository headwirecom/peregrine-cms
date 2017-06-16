<template>
    <div class="pathfield">
        <template v-for="item in pathSegments">
            /&nbsp;<admin-components-action
                v-bind:model="{
                    target: { path: item.path },
                    title: item.name,
                    command: 'selectPathInNav'
                }"></admin-components-action>&nbsp;
        </template>
    </div>
</template>

<script>
    export default {
        props: ['model']
        ,
        computed: {
            path: function() {
                var dataFrom    = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom(this.$root.$data, dataFrom)
                return node
            },
            pathSegments: function() {
                var segments = this.path.toString().split('/')
                var ret = []
                for(var i = 2; i < segments.length; i++) {
                    ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } )
                }
                return ret;
            }
        }
    }
</script>
