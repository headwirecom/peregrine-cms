<template>
    <div v-bind:data-per-path="model.path" v-html="model.text">
    </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

