<template>
  <themeclean-components-block v-bind:model="model">
    <themeclean-components-media v-bind:model="model"></themeclean-components-media>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>