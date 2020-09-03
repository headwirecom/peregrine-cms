<template>
  <div class="tab-component" :class="[model.classes]">
    <button class="toggle" @click="show = !show">{{ model.label }}</button>
    <div v-if="show" class="content">
      <template v-for="child in model.children">
        <component
            :is="child.component"
            :key="child.path"
            :model="child"/>
      </template>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    name: 'Tab',
    model: {
      type: Object,
      required: true
    }
  }
}
</script>
