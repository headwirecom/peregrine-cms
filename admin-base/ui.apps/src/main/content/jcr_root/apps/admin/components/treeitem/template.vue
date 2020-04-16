<template>
  <li class="tree-item" :class="{'expandable': hasChildren, 'is-open': isOpen}">
    <div :class="{title: hasChildren}" @click="toggle">
      {{ item.name }}
      <span v-if="hasChildren">[{{ isOpen ? '-' : '+' }}]</span>
    </div>
    <ul v-if="hasChildren" v-show="isOpen" class="content">
      <admin-components-treeitem
          v-for="(child, index) in item.children"
          :key="index"
          :item="child"/>
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
      hasChildren: function () {
        return this.item.children && this.item.children.length > 0;
      }
    },
    methods: {
      toggle: function () {
        if (this.hasChildren) {
          this.isOpen = !this.isOpen;
        }
      }
    }
  }
</script>
