<template>
  <themeclean-components-block v-bind:model="model">
    <div class="d-flex justify-content-between col-12 col-md-8">
      <a class="btn btn-sm btn-outline-primary percms-btn-pager" v-bind:href="$helper.pathToUrl(model.previous)">Prev</a>
      <a class="btn btn-sm btn-outline-primary percms-btn-pager" v-bind:href="$helper.pathToUrl(model.next)">Next</a>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

