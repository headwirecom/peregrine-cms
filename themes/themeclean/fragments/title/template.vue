<template>
  <themeclean-components-block v-bind:model="model">
    <div>
      <h1 v-html="model.title"></h1>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

