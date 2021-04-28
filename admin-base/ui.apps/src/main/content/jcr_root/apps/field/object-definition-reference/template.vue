<template>
  <!-- Switch -->
  <div class="wrap">
    <div v-if="!schema.preview" class="switch">
      <path-browser :model="model" :schema="schema"/>
    </div>
    <p v-else>{{ value }}</p>
  </div>
</template>

<script>
import PathBrowser from '../pathbrowser/template.vue';

export default {
  name: 'ObjectDefinitionReference',
  components: {PathBrowser},
  mixins: [VueFormGenerator.abstractField],
  props: ['model'],
}
</script>
