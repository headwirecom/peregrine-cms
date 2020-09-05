<template>
  <li>
    <a :title="title"
       v-on:click.stop.prevent="onClick">
      <icon :lib="iconLib" :icon="icon" class="editor-icon"/>
    </a>
  </li>
</template>

<script>
import {libValidator as iconLibValidator} from '../../../../../../js/validators/icon'
import {IconLib} from '../../../../../../js/constants'
import Icon from '../icon/template.vue'

export default {
  name: 'ExplorerPreviewNavItem',
  components: {Icon},
  props: {
    title: {
      type: String,
      required: false
    },
    icon: {
      type: String,
      required: true
    },
    iconLib: {
      type: String,
      default: IconLib.MATERIAL_ICONS,
      required: false,
      validator: iconLibValidator
    }
  },
  data: function () {
    return {};
  },
  computed: {},
  methods: {
    onClick() {
      this.$emit('click');
    }
  }
}
</script>
