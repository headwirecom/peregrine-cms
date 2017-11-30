<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 row justify-content-center article">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty"></div>
      <div v-bind:style="{flexBasis:`${model.mediawidth}%`}">
        <themeclean-components-media :model="model"></themeclean-components-media>
      </div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                //return !(this.model.imagesrc || this.model.videosrc)
                return this.$helper.areAllEmpty(this.model.imagesrc, this.model.videosrc)
            }
        }
    }
</script>