<template>
  <div class="tabs-wrapper" :class="[model.classes, direction]">
    <template v-for="child in model.children">
      <component
          :is="child.component"
          :key="child.path"
          :model="child"/>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    name: 'TabsWrapper',
    model: {
      type: Object,
      required: true,
      validator: (model) => {
        console.log(model)
        return true
      }
    }
  },
  data() {
    return {
      show: false
    }
  },
  computed: {
    direction() {
      if (this.model.direction) {
        return this.model.direction
      }

      return Direction.HORIZONTAL
    }
  },
  beforeMount() {
    if (this.model.show) {
      this.show = this.model.show //set initial show
    }
  }
}

const Direction = {
  HORIZONTAL: 'horizontal',
  VERTICAL: 'vertical'
}
</script>
