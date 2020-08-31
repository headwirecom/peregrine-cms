<template>
  <component
      :is="tag"
      tabindex="-1"
      @focusout="onFocusOut"
      class="materialize-drop-down"
      @click="$emit('toggle-click')">
    <a href="#" ref="dd" :data-activates="id">
      <slot></slot>
    </a>
    <div :id="id" class="dropdown-content" :class="{'scrollable': isScrollable}">
      <div v-if="!!$slots.header" class="header">
        <slot name="header" class="header"></slot>
      </div>
      <slot name="content"></slot>
      <template class="filter">
        <div v-if="searchable" class="filter-wrapper">
          <input
              type="text"
              v-model="filter"
              class="filter"
              placeholder="filter..."
              @click.stop="(event) => {event.target.focus()}"/>
        </div>
      </template>
      <ul v-if="items" class="items-list">
        <template v-for="(item, index) in filteredItems">
          <admin-components-dropdowndivider
              v-if="item === DropDown.DIVIDER"/>
          <li v-else
              :key="`item-${index}`"
              class="item"
              :class="[{disabled: item.disabled}, item.class? item.class() : null]"
              :title="item.title? item.title : false"
              @click="onItemClick(item, index)">
            <admin-components-icon v-if="item.icon" :icon="item.icon" :lib="item.iconLib"/>
            {{ item.label }}
            <div v-if="item.icon" class="center-keeper"></div>
          </li>
        </template>
      </ul>
    </div>
  </component>
</template>

<script>
import {DropDown, IconLib} from '../../../../../../js/constants'

export default {
  name: 'MaterializeDropDown',
  props: {
    items: {
      type: Array
    },
    tag: {
      type: String,
      default: 'div'
    },
    inDuration: {
      type: Number,
      default: 300
    },
    outDuration: {
      type: Number,
      default: 225
    },
    constrainWidth: {
      type: Boolean,
      default: true
    },
    hover: {
      type: Boolean,
      default: false
    },
    gutter: {
      type: Number,
      default: 0
    },
    belowOrigin: {
      type: Boolean,
      default: false
    },
    alignment: {
      type: String,
      default: 'left'
    },
    stopPropagation: {
      type: Boolean,
      default: false
    },
    searchable: {
      type: Boolean,
      default: false
    },
    onFocusOut: {
      type: Function,
      default() {
        if (this) {
          this.close()
        }
      }
    }
  },
  data() {
    return {
      IconLib,
      DropDown,
      $dd: null,
      maxNoScrollItems: 16,
      filter: ''
    }
  },
  computed: {
    id() {
      return `materailizedropdown-${this._uid}`
    },
    isScrollable() {
      return this.items && this.items.length > this.maxNoScrollItems
    },
    filteredItems() {
      if (this.filter) {
        const filtered = []

        this.items.forEach((item) => {
          if (item.name.indexOf(this.filter) > -1) {
            filtered.push(item)
          }
        })

        return filtered
      }
      return this.items
    }
  },
  beforeCreate() {
    $perAdminApp.loadComponent('admin-components-materializedropdownfilter')
  },
  mounted() {
    this.$dd = $(this.$refs.dd)
    this.$dd.dropdown({
      inDuration: this.inDuration,
      outDuration: this.outDuration,
      constrainWidth: this.constrainWidth,
      hover: this.hover,
      gutter: this.gutter,
      belowOrigin: this.belowOrigin,
      alignment: this.alignment,
      stopPropagation: this.stopPropagation
    })
  },
  methods: {
    onItemClick(item, index) {
      if (!item.disabled) {
        if (item.click) {
          item.click()
        } else {
          this.$emit('item-click', item, index)
        }
      }
    },
    close() {
      this.$dd.dropdown('close')
    }
  }
}
</script>
