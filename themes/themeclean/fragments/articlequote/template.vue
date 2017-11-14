<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 article">
      <hr class="line" v-bind:style="`width:${`${model.linewidth}%`};`">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="text-center" v-html="model.text"></div>
      <hr class="line" v-bind:style="`width:${`${model.linewidth}%`};`">
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