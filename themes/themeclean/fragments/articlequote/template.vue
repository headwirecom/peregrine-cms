<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 article">
      <hr class="line" v-if="model.blockquote == 'false'" v-bind:class="{
            'border-secondary': model.colorscheme === '',
            'border-dark' : model.colorscheme === 'light',
            'border-light' : model.colorscheme === 'dark'
        }" v-bind:style="`width:${`${model.linewidth}%`};`">
      <div class="text-center" v-html="model.text" v-bind:class="{
            'percms-blockquote': model.blockquote === 'true',
            'border-secondary': model.colorscheme === '',
            'border-dark' : model.colorscheme === 'light',
            'border-light' : model.colorscheme === 'dark'
        }"></div>
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <hr class="line" v-if="model.blockquote == 'false'"
      v-bind:class="{
            'border-secondary': model.colorscheme === '',
            'border-dark' : model.colorscheme === 'light',
            'border-light' : model.colorscheme === 'dark'
        }" v-bind:style="`width:${`${model.linewidth}%`};`">
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                //return !(this.model.text != '<p><br></p>')
                return this.$helper.areAllEmpty(this.model.text)
            }
        }
    }
</script>