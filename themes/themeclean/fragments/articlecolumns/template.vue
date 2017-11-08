<template>
  <themeclean-components-block v-bind:model="model">
    <div class="row col-12 col-md-8 d-flex justify-content-center">
      <div class="col-12 col-md p-0 pr-md-3" v-html="model.textleft"></div>
      <div class="col-12 col-md p-0 pl-md-3" v-html="model.textright"></div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

