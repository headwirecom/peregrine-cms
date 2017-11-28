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
        }" v-bind:style="`width:${model.textwidth}%;`">
          <h2 class="" v-if="model.showtitle == 'true'" v-html="model.title"></h2>
          <h4 class="" v-if="model.showsubtitle == 'true'" v-html="model.subtitle"></h4>
          <p v-if="model.showtext == 'true'" v-html="model.text"></p>
        </div>
      </div>
      <div class="col-12 p-0 d-flex" v-if="model.showbutton == 'true'"
      v-bind:class="`justify-content-sm-${model.alignbuttons}`">
        <a class="btn btn-secondary btn-lg" v-for="(item,i) in model.buttons"
        :key="i" v-bind:href="$helper.pathToUrl(item.buttonlink)" v-html="item.buttontext"
        v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};margin-left:${i == 0 ? 0 : '0.5rem'};margin-right:${i == model.buttons.length-1 ? 0: '0.5rem'};`"></a>
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

