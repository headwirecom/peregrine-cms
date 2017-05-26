<template>
    <div v-if="currentObject" class="asset-preview">
        <ul class="asset-info">
            <li>
                <span class="asset-name">created:</span>
                <span class="asset-value">April 1st, 2017</span>
            </li>
            <li>
                <span class="asset-name">modified:</span>
                <span class="asset-value">April 1st, 2017</span>
            </li>
            <li>
                <span class="asset-name">source:</span>
                <span class="asset-value">{{ currentObject.show }}</span>
            </li>
        </ul>
        <img v-if="isImage(currentObject.show)" v-bind:src="currentObject.show"/>
        <iframe v-else v-bind:src="currentObject.show"></iframe>
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
