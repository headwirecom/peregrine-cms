<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 border border-right-0 border-bottom-0 border-top-0 article">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <blockquote class="blockquote pl-3"
      v-html="model.text"></blockquote>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return !(this.model.text != '<p><br></p>')
            }
        }
    }
</script>