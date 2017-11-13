<template>
  <themeclean-components-block v-bind:model="model">
    <div>
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="col-12 col-md-8" v-html="model.text"></div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return !(this.model.text)
            }
        }
    }
</script>