<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 p-0 d-md-flex align-items-center justify-content-center flex-wrap article"
    v-bind:class="model.reverselayout === 'true' ? 'flex-row': 'flex-row-reverse'">
      <div class="col-sm-12 col-md">
        <themeclean-components-media v-bind:model="model"></themeclean-components-media>
      </div>
      <div class="col-sm-12 col-md" v-html="model.text"></div>
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
                //return !(this.model.text != '<p><br></p>' || this.model.imagesrc || this.model.videosrc)
                return this.$helper.areAllEmpty(this.model.text, this.model.imagesrc, this.model.videosrc)
            }
        }
    }
</script>