<template>
<div>
    <input v-model.lazy="path">
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
            }
        }
    }
</script>
