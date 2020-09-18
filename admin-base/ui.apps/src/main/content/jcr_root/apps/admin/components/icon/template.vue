<template>
  <i v-if="isFontAwesome" :class="['icon', 'fa', `fa-${icon}`]"></i>
  <i v-else-if="isMaterialIcons" class="icon material-icons">{{ icon }}</i>
  <div v-else-if="isPlainText" class="icon label" v-html="icon"></div>
</template>

<script>
import {IconLib} from '../../../../../../js/constants'
import {libValidator} from '../../../../../../js/validators/icon'

export default {
    name: 'Icon',
    props: {
      icon: {
        type: String,
        required: true
      },
      lib: {
        type: String,
        required: false,
        default: IconLib.MATERIAL_ICONS,
        validator: libValidator
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
