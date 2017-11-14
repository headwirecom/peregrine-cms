<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 d-md-flex align-items-center justify-content-center flex-wrap article">
      <themeclean-components-media v-bind:model="model"></themeclean-components-media>
      <div class="col-sm-12 col-md p-0 pt-3 pt-md-0 pl-md-3"
      v-html="model.text"></div>
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
                return !(this.model.text || this.model.imagesrc || this.model.videosrc)
            }
        }
    }
</script>