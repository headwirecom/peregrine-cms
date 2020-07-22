<template>
  <!-- IF -->
  <admin-components-materializedropdown
      v-if="computedItems && computedItems.length > 0"
      tag="button"
      class="btn"
      :class="{'active': active}"
      :title="computedTitle"
      :below-origin="true"
      :items="computedItems"
      @mousedown.native.prevent="() => {}">
    <template v-if="icon">
      <i v-if="iconLib === 'material-icons'" class="material-icons">{{ computedIcon }}</i>
      <i v-else-if="iconLib === 'font-awesome'" class="fa" :class="`fa-${computedIcon}`"></i>
    </template>
    <span class="caret-down"></span>
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
    <template v-if="icon">
      <i v-if="iconLib === 'material-icons'" class="material-icons">{{ computedIcon }}</i>
      <i v-else-if="iconLib === 'font-awesome'" class="fa" :class="`fa-${computedIcon}`"></i>
    </template>
    <div v-if="label" class="label" v-html="label"></div>
  </button>
</template>

<script>
  export default {
    name: 'RichToolbarBtn',
    props: {
      icon: [String, Function],
      iconLib: {type: String, default: 'font-awesome'},
      label: String,
      title: [String, Function],
      active: Boolean,
      items: [Array, Function]
    },
    computed: {
      computedTitle() {
        let title = this.title
        if (typeof title === 'function') {
          title = title()
        }
        return this.$i18n(title)
      },
      computedIcon() {
        let icon = this.icon
        if (typeof icon === 'function') {
          icon = icon()
        }
        return icon
      },
      computedItems() {
        let items = this.items
        if (typeof items === 'function') {
          items = items()
        }
        return items
      }
    }
  }
</script>
