<template>
  <themeclean-components-block v-bind:model="model">
    <div v-bind:style="`height:${model.height}vh;`"></div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

