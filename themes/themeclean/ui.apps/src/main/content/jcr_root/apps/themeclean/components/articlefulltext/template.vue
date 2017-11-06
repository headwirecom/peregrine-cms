<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8" v-html="model.text"></div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>