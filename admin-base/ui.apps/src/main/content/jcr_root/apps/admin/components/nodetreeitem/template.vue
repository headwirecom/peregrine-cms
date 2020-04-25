<template>
  <li class="page-tree-item" :class="{'expandable': item.hasChildren, 'is-open': isOpen}">
    <div class="title" :class="{'is-selected': this.isSelected}" @click.stop="editNode">
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
      <admin-components-nodetreeitem
          v-for="(child, index) in filteredChildren"
          :key="`page-tree-item-${child.path}`"
          :item="child"
          :supported-resource-types="supportedResourceTypes"
          @edit-node="$emit('edit-node')"/>
    </ul>
  </li>
</template>

<script>
  import {capitalizeFirstLetter} from '../../../../../../js/utils'

  export default {
    name: 'TreeItem',
    props: {
      item: Object,
      supportedResourceTypes: Array
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
      },
      section() {
        return this.currentPath.split('/')[3] || null
      },
      sectionSingular() {
        return capitalizeFirstLetter(this.section.slice(0, -1)) || null
      },
      filteredChildren() {
        if (this.item.children) {
          return this.item.children.filter((ch) => {
            return this.supportedResourceTypes.indexOf(ch.resourceType) >= 0
          })
        } else {
          return []
        }
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

        this.isOpen = !this.isSelected && partCurrPathArr.join('/') === this.item.path
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
      editNode() {
        if (!this.isSelected && this.section) {
          $perAdminApp.stateAction(`edit${this.sectionSingular}`, this.item.path)
        }
        this.$emit('edit-node')
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
