<template>
  <div class="wrapper">
    <div v-if="!schema.preview" class="switch">
      <path-browser-field
          :model="pathBrowserFieldModel"
          :schema="pathBrowserFieldSchema"
          @select="onSelect"/>
    </div>
    <p v-else>{{ value }}</p>
  </div>
</template>

<script>
import {IconLib, PathBrowser} from '../../../../../js/constants'
import {getBasePath} from '../../../../../js/mixins'

import PathBrowserField from '../pathbrowser/template.vue'


export default {
  name: 'ObjectDefinitionReference',
  props: ['model'],
  components: {PathBrowserField},
  mixins: [VueFormGenerator.abstractField, getBasePath],
  computed: {
    pathBrowserFieldModel() {
      return Object.assign({}, this.model, {
        buttonIcon: {icon: '{&#8230;}', lib: IconLib.PLAIN_TEXT},
      })
    },
    pathBrowserFieldSchema() {
      return this.schema
    }
  },
  created() {
    this.schema.browserType = PathBrowser.Type.OBJECT_DEFINITION
    this.schema.browserRoot = this.getBasePath() + PathBrowser.Root.OBJECT_DEFINITION
    this.schema.browserOptions = {withLink: false}
  },
  methods: {
    onSelect(selectedPath) {
      console.log('onSelect:', selectedPath)
    }
  }
}
</script>

<style scoped>
.wrapper .switch .wrap {
  display: flex;
  flex-direction: row;
}

.wrapper .switch .wrap::v-deep .btn-flat {
  height: 3rem;
  margin-right: 0 !important;
}

.wrapper .switch .wrap::v-deep .btn-flat .icon {
  font-weight: bolder;
  color: #000000;
}
</style>