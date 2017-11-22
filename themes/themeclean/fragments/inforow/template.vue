<template>
  <themeclean-components-block v-bind:model="model">
    <div class="row col-12 col-md-10 align-items-center justify-content-between"
    v-bind:class="model.reverselayout">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="col-12 col-md-auto p-0"
      v-if="model.showbutton == 'true'">
        <h2 class="" v-if="model.showtitle == 'true'" v-html="model.title"></h2>
        <h4 class="" v-if="model.showsubtitle == 'true'" v-html="model.subtitle"></h4>
      </div>
      <div class="col-12 col-md-auto p-0 d-flex justify-content-end">
        <a class="btn btn-primary btn-lg" v-for="(item,i) in model.buttons" :key="i"
        v-bind:href="$helper.pathToUrl(item.buttonlink)" v-html="item.buttontext"
        v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};margin-left:${i == 0 ? 0 : '0.5rem'};margin-right:${i == model.buttons.length-1 ? 0: '0.5rem'};`"></a>
      </div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                //return !(this.model.showtitle === 'true' || this.model.showsubtitle === 'true' || this.model.showbutton === 'true')
                return this.$helper.areAllEmpty(this.model.showtitle === 'true' , this.model.showsubtitle === 'true' , this.model.showbutton === 'true')
                
            }
        }
    }
</script>