<template>
  <i v-if="isFontAwesome" :class="['fa', `fa-${icon}`]"></i>
  <i v-else-if="isMaterialIcons" class="material-icons">{{ icon }}</i>
  <div v-else-if="isPlainText" class="label" v-html="icon"></div>
</template>

<script>
  import {IconLib} from '../../../../../../js/constants'

  export default {
    props: {
      icon: {
        type: String,
        required: true
      },
      lib: {
        type: String,
        required: true,
        default: IconLib.MATERIAL_ICONS
      }
    },
    computed: {
      isFontAwesome() {
        return this.lib === IconLib.FONT_AWESOME
      },
      isMaterialIcons() {
        return this.lib === IconLib.MATERIAL_ICONS
      },
      isPlainText() {
        return this.lib === IconLib.PLAIN_TEXT
      }
    }
  }
</script>
