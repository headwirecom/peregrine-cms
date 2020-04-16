<template>
  <li class="page-tree-item" :class="{'expandable': item.hasChildren, 'is-open': isOpen}">
    <div class="title" :class="{'is-selected': this.isSelected}">
      <i v-if="item.hasChildren" class="material-icons" @click.stop="toggle">{{ expandIcon }}</i>
      <i v-else class="icon-placeholder"></i>
      <i class="material-icons" @click.stop="editPage">description</i>
      {{ item.name }}
    </div>
    <ul v-if="item.hasChildren" v-show="isOpen" class="content">
      <admin-components-pagetreeitem
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
      expandIcon() {
        return this.isOpen ? 'keyboard_arrow_down' : 'keyboard_arrow_right'
      },
      currentPath() {
        return $perAdminApp.getView().pageView.path || ''
      },
      isSelected() {
        return this.item.path === this.currentPath
      }
    },
    watch: {
      currentPath(newVal, oldVal) {
        this.initIsOpen()
      }
    },
    mounted() {
      this.initIsOpen()
    },
    methods: {
      initIsOpen() {
        if (this.currentPath.startsWith(this.item.path) && !this.isSelected) {
          this.isOpen = true
        }
      },
      toggle() {
        if (this.item.hasChildren) {
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
