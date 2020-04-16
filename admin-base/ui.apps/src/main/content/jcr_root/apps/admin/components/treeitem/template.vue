<template>
  <li class="tree-item" :class="{'expandable': hasChildren, 'is-open': isOpen}">
    <div :class="{title: hasChildren}" @click="toggle" @dblclick="editPage">
      {{ item.name }}
      <span v-if="hasChildren">[{{ isOpen ? '-' : '+' }}]</span>
    </div>
    <ul v-if="hasChildren" v-show="isOpen" class="content">
      <admin-components-treeitem
          v-for="(child, index) in item.children"
          :key="index"
          :item="child"
          @edit-page="$emit('edit-page')"/>
    </ul>
  </li>
</template>

<script>
  export default {
    name: 'TreeItem',
    props: {
      item: Object
    },
    data() {
      return {
        isOpen: false
      }
    },
    computed: {
      hasChildren() {
        return this.item && this.item.children && this.item.children.length > 0
      }
    },
    methods: {
      toggle() {
        if (this.hasChildren) {
          this.isOpen = !this.isOpen
        }
      },
      editPage() {
        $perAdminApp.stateAction('editPage', this.item.path)
        this.$emit('edit-page')
      }
    }
  }
</script>
