<template>
  <div class="btn-group" :class="[`group-${vLabel}`]">
    <admin-components-materializedropdown
        v-if="collapse && items && items.length > 1"
        tag="button"
        class="btn"
        :class="[{'active': active}]"
        :title="vLabel"
        :below-origin="true"
        :items="vItems"
        :searchable="searchable"
        @mousedown.native.prevent="() => {}"
        @toggle-click="$emit('toggle-click')"
        @item-click="$emit('click', {btn: $event})">
      <admin-components-icon v-if="icon" :icon="vIcon" :lib="iconLib"/>
      <span class="caret-down"></span>
    </admin-components-materializedropdown>

    <template v-else>
      <template v-for="(btn, i) in items">
        <admin-components-richtoolbargroup
            v-if="btn.items && btn.items.length > 1"
            :key="`rich-toolbar-sub-group-${i}`"
            :icon="btn.icon"
            :iconLib="btn.iconLib"
            :collapse="btn.collapse"
            :label="btn.label"
            :title="btn.title"
            :active="btn.active"
            :items="btn.items"
            :class="btn.class"
            @click="$emit('click', $event)"/>
        <admin-components-richtoolbarbtn
            v-else
            :key="getButtonKey(btn, i)"
            :items="btn.items"
            :icon="btn.icon"
            :icon-lib="btn.iconLib"
            :label="btn.label"
            :class="btn.class"
            :title="$i18n(btn.title)"
            :active="btn.isActive? btn.isActive() : false"
            @click="$emit('click', {btn})"/>
      </template>
    </template>
  </div>
</template>

<script>
import {IconLib} from '../../../../../../js/constants'
import {libValidator as iconLibValidator} from '../../../../../../js/validators/icon'

export default {
  name: 'RichToolbarGroup',
  props: {
    icon: {
      type: [String, Function]
    },
    iconLib: {
      type: String,
      default: IconLib.MATERIAL_ICONS,
      validator: iconLibValidator
    },
    collapse: {
      type: Boolean,
      default: false
    },
    label: {
      type: [String, Function]
    },
    active: {
      type: Boolean
    },
    items: {
      type: [Array, Function]
    },
    searchable: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    vLabel() {
      let label = this.label
      if (typeof label === 'function') {
        label = label()
      }
      return label
    },
    vIcon() {
      let icon = this.icon
      if (typeof icon === 'function') {
        icon = icon()
      }
      return icon
    },
    vItems() {
      let items = this.items
      if (typeof items === 'function') {
        items = items()
      }
      return items
    }
  },
  methods: {
    getButtonKey(btn, index) {
      let key = `btn-${index}-${btn.title}`
      if (!btn.isActive || btn.isActive() !== null) {
        key += `-${this.$vnode.key}`
      }
      return key
    }
  }
}
</script>
