<template>
    <div v-if="currentObject">
        {{currentObject.show}}
        <pre>{{currentObject.data}}</pre>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentObject: function() {
                return $perAdminApp.getNodeFromView("/state/tools/object")
            }
        }
    }
</script>
