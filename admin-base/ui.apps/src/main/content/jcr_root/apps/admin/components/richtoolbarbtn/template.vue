<template>
  <!-- IF -->
  <admin-components-materializedropdown
      v-if="items && items.length > 0"
      tag="button"
      class="btn"
      :class="{'active': active}"
      :title="$i18n(title)"
      :below-origin="true"
      :items="items"
      @mousedown.native.prevent="() => {}">
    <i class="material-icons">{{ icon }}</i><span class="caret-down"></span>
  </admin-components-materializedropdown>
  <!-- ELSE -->
  <button
      v-else
      class="btn"
      :class="{'active': active}"
      :title="$i18n(title)"
      @mousedown.prevent="() => {}"
      @click="$emit('click')">
    <i class="material-icons">{{ icon }}</i>
  </button>
</template>

<script>
  export default {
    name: 'RichToolbarBtn',
    props: {
      icon: String,
      title: String,
      active: Boolean,
      items: Array
    }
  }
</script>
