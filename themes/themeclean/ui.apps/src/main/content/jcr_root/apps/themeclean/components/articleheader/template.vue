<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8" v-bind:class="model.textalign">
      <h2 class="col-12 p-0 pb-3" v-if="model.showtitle == 'true'">{{model.title}}</h2>
      <h3 class="col-12 p-0 pb-3" v-if="model.showsubtitle == 'true'">{{model.subtitle}}</h3>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>