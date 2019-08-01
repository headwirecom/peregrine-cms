<template>
    <hr class="hr" :data-per-path="model.path"/>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

<style scoped>
    .hr {
        width: 100%;
    }
</style>
