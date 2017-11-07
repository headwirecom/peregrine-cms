<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 row justify-content-center">
      <themeclean-components-media :model="model"></themeclean-components-media>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>