<template>
  <themeclean-components-block v-bind:model="model">
    <div>
      <div v-bind:style="`height:${model.height}vh;`"></div>
      <div v-if="isEditAndEmpty">height needs to be greater than 0</div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return this.$helper.areAllEmpty(this.model.height > 0)
            }
        }
    }
</script>

