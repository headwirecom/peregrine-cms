<template>
<p v-html="model.text"></p>
</template>

<script>
export default {
    props: [ 'model']
}
</script>
