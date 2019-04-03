<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-10 align-items-center">
      <div class="d-flex col-12 p-0" v-bind:class="{
            'justify-content-start': model.aligncontent === 'left',
            'justify-content-center': model.aligncontent === 'center',
            'justify-content-end': model.aligncontent === 'right'
        }">
        <div class="percms-w-sm-100" v-bind:class="{
            'text-left': model.aligncontent === 'left',
            'text-center': model.aligncontent === 'center',
            'text-right': model.aligncontent === 'right',
            'percms-text-large': model.isprimary === 'true'
        }" v-bind:style="`width:${model.textwidth}%;`">
          <h2 class v-if="model.showtitle === 'true'" v-html="model.title"></h2>
          <h4 class v-if="model.showsubtitle === 'true'" v-html="model.subtitle"></h4>
          <p v-if="model.showtext === 'true'" v-html="model.text"></p>
        </div>
      </div>
      <div class="col-12 d-flex flex-wrap p-0 justify-content-center" v-if="model.showbutton == 'true'"
      v-bind:class="{
            'justify-content-md-start': model.alignbuttons === 'start',
            'justify-content-md-center': model.alignbuttons === 'center',
            'justify-content-md-end': model.alignbuttons === 'end'
        }">
        <a class="btn m-2" v-for="(item,i) in model.buttons" :key="i" v-bind:href="$helper.pathToUrl(item.buttonlink)"
        v-bind:class="{
            'btn-lg': model.buttonsize === 'large',
            'btn-sm': model.buttonsize === 'small',
            'btn-primary': item.buttoncolor === 'primary',
            'btn-secondary': item.buttoncolor === 'secondary',
            'btn-success': item.buttoncolor === 'success',
            'btn-danger': item.buttoncolor === 'danger',
            'btn-warning': item.buttoncolor === 'warning',
            'btn-info': item.buttoncolor === 'info',
            'btn-light': item.buttoncolor === 'light',
            'btn-dark': item.buttoncolor === 'dark'
        }" v-html="item.buttontext" v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`"></a>
      </div>
      <div v-if="isEditAndEmpty">no content defined for component</div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return this.$helper.areAllEmpty(this.model.showtitle === 'true', this.model.showsubtitle === 'true', this.model.showtext === 'true', this.model.showbutton === 'true')
            }
        }
    }
</script>

