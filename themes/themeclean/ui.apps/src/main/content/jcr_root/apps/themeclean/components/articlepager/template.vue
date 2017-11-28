<template>
  <themeclean-components-block v-bind:model="model">
    <div class="d-flex justify-content-between col-12 col-md-8">
      <a class="btn btn-sm btn-outline-primary percms-btn-pager" v-if="model.previous == 'unknown'">{{model.prevlabel}}</a>
      <a class="btn btn-sm btn-outline-primary percms-btn-pager"
      v-if="model.previous != 'unknown'" v-bind:href="$helper.pathToUrl(model.previous)">{{model.prevlabel}}</a>
      <a class="btn btn-sm btn-outline-primary percms-btn-pager"
      v-if="model.next == 'unknown'">{{model.nextlabel}}</a>
      <a class="btn btn-sm btn-outline-primary percms-btn-pager"
      v-if="model.next != 'unknown'" v-bind:href="$helper.pathToUrl(model.next)">{{model.nextlabel}}</a>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

