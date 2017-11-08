<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 d-md-flex align-items-center justify-content-center flex-wrap">
      <themeclean-components-media v-bind:model="model"></themeclean-components-media>
      <div class="col-sm-12 col-md p-0 pt-3 p-md-3"
      v-html="model.text"></div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>