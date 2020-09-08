<template>
  <button
      class="btn"
      :class="{'active': active}"
      :title="vTitle"
      @mousedown.prevent="() => {}"
      @click="$emit('click')">
    <admin-components-icon v-if="vIcon" :icon="vIcon" :lib="iconLib"/>
  </button>
</template>

<script>
import {IconLib} from '../../../../../../js/constants'
import {libValidator as iconLibValidator} from '../../../../../../js/validators/icon'

export default {
    name: 'RichToolbarBtn',
    props: {
      icon: [String, Function],
      iconLib: {
        type: String,
        default: IconLib.MATERIAL_ICONS,
        validator: iconLibValidator
      },
      label: String,
      title: [String, Function],
      active: Boolean
    },
    computed: {
      vTitle() {
        let title = this.title
        if (typeof title === 'function') {
          title = title()
        }
        return this.$i18n(title)
      },
      vIcon() {
        let icon = this.icon
        if (typeof icon === 'function') {
          icon = icon()
        }
        return icon
      }
    }
  }
</script>
