<template>
  <button class="handle" :title="title">
    <icon v-if="model.icon" :icon="model.icon" :lib="model.iconLib"/>
    {{ model.label }}
  </button>
  <!-- content is rendered in TabsWrapper-->
</template>

<script>
import Icon from '../icon/template.vue'

export default {
  name: 'Tab',
  components: {
    Icon
  },
  props: {
    model: {
      type: Object,
      required: true
    }
  },
  computed: {
    title() {
      return this.model.title? this.model.title : this.model.label
    }
  }
}
</script>
