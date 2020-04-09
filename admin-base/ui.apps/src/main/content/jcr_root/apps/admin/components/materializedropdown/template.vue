<template>
  <component :is="tag" tabindex="-1" @focusout="onFocusOut">
    <a href="#" ref="dd" :data-activates="id">
      <slot></slot>
    </a>
    <ul :id="id" class="dropdown-content">
      <li v-for="(item, index) in items"
          :key="`item-${index}`"
          class="item"
          :class="{disabled: item.disabled}"
          :title="item.title? $i18n(item.title) : false"
          @click="onItemClick(item, index)">
        {{ $i18n(item.label) }}
      </li>
    </ul>
  </component>
</template>

<script>
  export default {
    props: {
      items: {
        type: Array,
        required: true
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
    },
    data() {
      return {
        $dd: null
      }
    },
    computed: {
      id() {
        return `materailizedropdown-${this._uid}`
      }
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
          this.$emit('item-click', item, index)
        }
      },
      onFocusOut(event) {
        this.$dd.dropdown('close')
      }
    }
  }
</script>
