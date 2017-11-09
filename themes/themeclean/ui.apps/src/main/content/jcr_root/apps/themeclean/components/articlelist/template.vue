<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8">
      <component class="col-12" :is="model.numberedlist === 'true' ? 'ol':'ul'">
        <li v-for="(item,i) in model.listitems" :key="i" v-html="item.text"></li>
      </component>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>