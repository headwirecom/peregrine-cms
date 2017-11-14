<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 row justify-content-center">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <themeclean-components-media :model="model"></themeclean-components-media>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return !(this.model.imagesrc || this.model.videosrc)
            }
        }
    }
</script>