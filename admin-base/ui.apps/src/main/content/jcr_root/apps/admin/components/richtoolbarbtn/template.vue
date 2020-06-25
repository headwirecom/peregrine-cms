<template>
  <!-- IF -->
  <admin-components-materializedropdown
      v-if="items && items.length > 0"
      tag="button"
      class="btn"
      :class="{'active': active}"
      :title="computedTitle"
      :below-origin="true"
      :items="items"
      @mousedown.native.prevent="() => {}">
    <i v-if="icon" class="material-icons">{{ icon }}</i><span class="caret-down"></span>
    <div v-if="label" class="label" v-html="label"></div>
  </admin-components-materializedropdown>
  <!-- ELSE -->
  <button
      v-else
      class="btn"
      :class="{'active': active}"
      :title="computedTitle"
      @mousedown.prevent="() => {}"
      @click="$emit('click')">
    <i v-if="icon" class="material-icons">{{ icon }}</i>
    <div v-if="label" class="label" v-html="label"></div>
  </button>
</template>

<script>
  export default {
    name: 'RichToolbarBtn',
    props: {
      icon: String,
      label: String,
      title: [String, Function],
      active: Boolean,
      items: Array
    },
    computed: {
      computedTitle() {
        let title = this.title
        if (typeof this.title === 'function') {
          title = title()
        }
        return this.$i18n(title)
      }
    }
  }
</script>
