<template>
  <div class="tabs-wrapper" :class="[model.classes, direction]">
    <div class="handles-wrapper">
      <template v-for="(child, index) in model.children">
        <component
            :is="child.component"
            :key="child.path"
            :model="child"
            :class="{active: activeTabIndex === index}"
            @click.native="setActiveTabIndex(index)"/>
      </template>
    </div>
    <div :class="`content active-tab-index-${activeTabIndex}`">
      <template v-for="activeTabChild in activeTab.children">
        <component
            :is="activeTabChild.component"
            :key="activeTabChild.path"
            :model="activeTabChild"/>
      </template>
    </div>
  </div>
</template>

<script>
import {modelValidator} from '../../../../../../js/validators/tabsWrapper'

export default {
  name: 'TabsWrapper',
  props: {
    model: {
      type: Object,
      required: true,
      validator: modelValidator
    }
  },
  data() {
    return {
      activeTabIndex: 0
    }
  },
  computed: {
    direction() {
      if (this.model.direction) {
        return this.model.direction
      }

      return 'horizontal'
    },
    activeTab(){
      return this.model.children[this.activeTabIndex]
    }
  },
  methods: {
    setActiveTabIndex(index) {
      this.activeTabIndex = index
    }
  }
}
</script>
