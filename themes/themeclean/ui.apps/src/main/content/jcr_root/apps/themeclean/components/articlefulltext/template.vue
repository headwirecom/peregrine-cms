<template>
  <themeclean-components-block v-bind:model="model">
    <div class="article col-12 col-md-8">
      <div v-html="model.text"></div>
      <div v-if="isEditAndEmpty">no content defined for component</div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                //return this.model.text == '<p><br></p>'
                return this.$helper.areAllEmpty(this.model.text)
            }
        }
    }
</script>