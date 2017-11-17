<template>
  <themeclean-components-block v-bind:model="model">
    <div v-bind:style="{flexBasis:`${model.mediawidth}%`}">
      <themeclean-components-media v-bind:model="model"></themeclean-components-media>
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty"></div>
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
	                return this.$helper.areAllEmpty(this.model.imagesrc , this.model.videosrc)
	            }
        }
    }
</script>