<template>
  <li>
    <div
        :class="{bold: isFolder}"
        @click="toggle">
      {{ item.name }}
      <span v-if="isFolder">[{{ isOpen ? '-' : '+' }}]</span>
    </div>
    <ul v-show="isOpen" v-if="isFolder">
      <admin-components-treeitem
          class="item"
          v-for="(child, index) in item.children"
          :key="index"
          :item="child"
          @add-item="$emit('add-item', $event)"/>
      <li class="add" @click="$emit('add-item', item)">+</li>
    </ul>
  </li>
</template>

<script>
  export default {
    name: 'TreeItem',
    props: {
      item: Object
    },
    data: function () {
      return {
        isOpen: false
      };
    },
    computed: {
      isFolder: function () {
        return this.item.children && this.item.children.length;
      }
    },
    methods: {
      toggle: function () {
        if (this.isFolder) {
          this.isOpen = !this.isOpen;
        }
      }
    }
  }
</script>

<style scoped>

</style>