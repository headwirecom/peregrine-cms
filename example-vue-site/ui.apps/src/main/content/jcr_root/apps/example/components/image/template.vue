<template>
    <div v-bind:data-per-path="model.path">
        <img  v-bind:src="image" v-bind:alt="model.caption" style="max;width: 100%;">
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            image: function() {
                if(this.model.imagePath !== '') {
                    return this.model.imagePath
                }
                if(window.parent && window.parent.$perAdminApp) {
                    return '/empty'
                }
            }
        }

    }
</script>

<style>
</style>
