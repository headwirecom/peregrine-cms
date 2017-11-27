<template>
  <themeclean-components-block v-bind:model="model">
    <div class="w-100 d-flex justify-content-between">
      <a class="btn btn-outline-primary" v-bind:href="$helper.pathToUrl(model.previous)">Prev</a>
      <a class="btn btn-outline-primary" v-bind:href="$helper.pathToUrl(model.next)">Next</a>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

