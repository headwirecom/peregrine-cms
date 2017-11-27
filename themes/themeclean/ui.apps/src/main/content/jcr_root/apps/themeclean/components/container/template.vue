<template>
  <div class="container" v-bind:data-per-path="model.path">
    <pagerender-vue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'after'  }"></pagerender-vue-components-placeholder>
    <div v-for="(item,i) in model.children"
    :key="i">
      <component v-bind:is="child.component" v-bind:model="child"></component>
    </div>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

