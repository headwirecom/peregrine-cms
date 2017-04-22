<template>
<div>
    <input v-model.lazy="path.value">
</div>
</template>

<script>
    export default {
        props: ['model']
        ,
        data: function() {
            var dataDefault = this.model.dataDefault
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
            if(!node.value) {
                this.$root.$set(node, 'value', dataDefault.toString())
                loadData('/pages', dataDefault.toString())
            }

            return { path: node }
        }
    }
</script>
