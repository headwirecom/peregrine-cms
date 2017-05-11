<template>
<p v-html="text" v-bind:data-per-path="model.path"></p>
</template>

<script>
export default {
    props: [ 'model'],
    computed: {
        text: function() {
            if(this.model.text !== '') {
                return this.model.text
            }
            if(window.parent && window.parent.$perAdminApp) {
                return 'empty'
            }
        }
    }
}
</script>
