<template>
  <themeclean-components-block v-bind:model="model">
    <div class="row col-12 d-flex justify-content-center">
      <div class="col-12 col-md-4" v-html="model.textleft"></div>
      <div class="col-12 col-md-4" v-html="model.textright"></div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

