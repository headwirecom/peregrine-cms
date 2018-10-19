<template>
  <themeclean-components-block v-bind:model="model">
    <div class="row col-12 col-md-8 p-0 d-flex align-items-center justify-content-center flex-wrap article"
    v-bind:class="model.mediaposition === 'after' ? 'flex-row-reverse': 'flex-row'">
      <div class="col-12 col-lg-auto pb-3 p-lg-0 px-lg-3" v-bind:style="{width: `${model.mediawidth}%`}">
        <themeclean-components-media v-bind:model="model"></themeclean-components-media>
      </div>
      <div class="col-12 col-md" v-html="model.text"></div>
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