<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 border border-right-0 border-bottom-0 border-top-0">
      <blockquote class="blockquote pl-3" v-html="model.text"></blockquote>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>