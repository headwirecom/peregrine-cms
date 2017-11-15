<template>
  <themeclean-components-block v-bind:model="model">
    <div class="row col-12 col-md-10 align-items-center justify-content-start">
      <div class="col-12 p-0">
        <h2 class="text-uppercase" v-if="model.showtitle == 'true'"
        v-html="model.title"></h2>
        <h4 class="" v-if="model.showsubtitle == 'true'" v-html="model.subtitle"></h4>
        <p v-if="model.showtext == 'true'" v-html="model.text"></p>
      </div>
      <div class="col-12 p-0" v-if="model.showbutton == 'true'">
        <a class="btn btn-secondary btn-lg m-2" v-for="(item,i) in model.buttons"
        :key="i" v-bind:href="$helper.pathToUrl(item.buttonlink)" v-html="item.buttontext"
        v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`"></a>
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
                return !(
                  this.model.showtitle === 'true' || 
                  this.model.showsubtitle === 'true' || 
                  this.model.showsubtext === 'true' || 
                  this.model.showbutton === 'true'
                )
            }
        }
    }
</script>