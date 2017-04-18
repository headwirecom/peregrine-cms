<template>
<div v-html="model.text"></div>
</template>

<script>
export default {
    props: [ 'model']
}
</script>
