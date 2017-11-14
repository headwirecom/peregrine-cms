<template>
  <themeclean-components-block v-bind:model="model">
    <div class="row col-12 col-md-8 d-flex justify-content-center article">
      <div class="col-12 col-md p-0 pr-md-3" v-html="model.textleft"></div>
      <div class="col-12 col-md p-0 pl-md-3" v-html="model.textright"></div>
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                //return this.model.textleft == '<p><br></p>' && this.model.textright == '<p><br></p>'
                // return !(this.model.textleft != '<p><br></p>' || this.model.textright != '<p><br></p>')
                return this.$helper.areAllEmpty( this.model.textleft, this.model.textright )
            }
        }
    }
</script>

