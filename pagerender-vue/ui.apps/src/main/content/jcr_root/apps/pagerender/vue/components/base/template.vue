<template>
<p v-html="model.text" v-bind:data-per-path="model.path"></p>
</template>

<script>
export default {
    props: [ 'model']
}
</script>
