<template>
  <li class="page-tree-item" :class="{'expandable': item.hasChildren, 'is-open': isOpen}">
    <div class="title" :class="{'is-selected': this.isSelected}" @click.stop="editPage">
      <template>
        <i v-if="item.hasChildren" class="material-icons hover" @click.stop.prevent="toggle">
          {{ expandIcon }}
        </i>
        <i v-else class="icon-placeholder"></i>
      </template>
      <i class="material-icons">description</i>
      {{ item.name }}
    </div>
    <ul v-if="item.hasChildren" v-show="isOpen" class="content">
      <admin-components-pagetreeitem
          v-for="(child, index) in item.children"
          :key="`page-tree-item-${child.path}`"
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
      },
      'item.children'(newVal) {
        if (!newVal || newVal.length <= 0) {
          this.isOpen = false
        }
      }
    },
    mounted() {
      this.initIsOpen()
    },
    methods: {
      initIsOpen() {
        const currPathArr = this.currentPath.split('/')
        const pathArr = this.item.path.split('/')
        const partCurrPathArr = currPathArr.splice(0, pathArr.length)

        if (!this.isSelected && partCurrPathArr.join('/') === this.item.path) {
          this.isOpen = true
        } else {
          this.isOpen = false
        }
      },
      toggle() {
        if (this.item.hasChildren) {
          if (!this.isOpen && (!this.item.children || this.item.children.length <= 0)) {
            this.loadChildren().then(() => {
              this.isOpen = !this.isOpen
            })
          } else {
            this.isOpen = !this.isOpen
          }
        }
      },
      editPage() {
        if (!this.isSelected) {
          $perAdminApp.stateAction('editPage', this.item.path)
        }
        this.$emit('edit-page')
      },
      loadChildren() {
        return $perAdminApp.stateAction('loadToolsNodesPath', {
          selected: this.item.path,
          path: '/state/tools/pages'
        })
      }
    }
  }
</script>
