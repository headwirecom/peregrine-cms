<template>
  <div v-bind:data-per-path="model.path" v-html="text">text</div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            text() {
                return ( this.model.text ? this.model.text : 'empty' )
            }
        }
    }
</script>

