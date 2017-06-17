<template>
    <div v-if="isEditMode" style="border: 1px solid #c0c0c0; clear: both; padding: 4px; margin: 4px; text-align: center; width: 100%;"
        v-on:allowDrop="allowDrop" v-on:drop="drop" v-bind:data-per-path="model.path" data-per-droptarget="true">
        {{componentName}}
    </div>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        isEditMode: function() {
            if(window.parent) {
                if(window.parent.$perAdminApp && window.parent !== window) {
                    return !window.parent.$perAdminApp.isPreviewMode()
                }
            }
            return false
        },
        componentName: function() {
            return this.model.component.split('-').pop()
        }
    },
    methods: {
        allowDrop: function(e) {
            e.preventDefault()
        },
        drop: function(e) {
            alert('component drop')
        },
        edit: function(e) {
            alert('edit')
        }
    }
}
</script>
