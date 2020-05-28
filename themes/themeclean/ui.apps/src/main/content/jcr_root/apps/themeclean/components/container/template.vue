<template>
  <div v-bind:data-per-path="model.path">
    <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'before' }"></pagerendervue-components-placeholder>
    <template v-for="child in model.children">
      <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>
    <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'after' }"></pagerendervue-components-placeholder>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

