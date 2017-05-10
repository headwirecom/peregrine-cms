<template>
    <div v-if="currentObject">
        <img v-if="isImage(currentObject.show)" v-bind:src="currentObject.show" width="400px"/>
        <iframe v-else v-bind:src="currentObject.show" width="400px"></iframe>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentObject: function () {
                return $perAdminApp.getNodeFromView("/state/tools/asset")
            }
        },
        methods: {
            isImage: function(path) {
                if(!path) return false
                let ext = path.split('.').pop().toLowerCase()
                return ['png','jpeg','jpg','gif','tiff', 'svg'].indexOf(ext) >= 0
            }
        }
    }
</script>
