<template>
  <themeclean-components-block v-bind:model="model">
    <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
    <themeclean-components-media v-bind:model="model"></themeclean-components-media>
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