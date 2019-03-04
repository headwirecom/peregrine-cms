<template>
  <themeclean-components-block v-bind:model="model">
    <div class="d-flex flex-column col-12 col-md-10 justify-content-between align-items-center"
    v-bind:class="{
            'flex-md-row-reverse': model.buttonside === 'left',
            'flex-md-row': model.buttonside === 'right',
        }">
      <div class v-bind:class="{
            'text-left': model.aligncontent === 'left',
            'text-center': model.aligncontent === 'center',
            'text-right': model.aligncontent === 'right',
            'percms-text-large': model.isprimary === 'true'
        }" v-bind:style="`flex-basis:${model.textwidth}%;`">
        <h2 class v-if="model.showtitle === 'true'" v-html="model.title"></h2>
        <h4 class v-if="model.showsubtitle === 'true'" v-html="model.subtitle"></h4>
        <p v-if="model.showtext === 'true'" v-html="model.text"></p>
      </div>
      <div class="d-flex flex-wrap justify-content-center" v-if="model.showbutton == 'true'"
      v-bind:class="{
            'justify-content-md-end': model.buttonside === 'right',
            'justify-content-md-start': model.buttonside === 'left',
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

