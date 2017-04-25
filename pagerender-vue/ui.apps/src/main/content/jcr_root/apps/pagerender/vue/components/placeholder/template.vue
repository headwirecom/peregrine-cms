<template>
    <div v-if="isEditMode" style="border: 1px solid #c0c0c0; width: 95%; padding: 4px; margin: 4px; text-align: center; "
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
                if(window.parent.perAdminView) {
                    return true
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
