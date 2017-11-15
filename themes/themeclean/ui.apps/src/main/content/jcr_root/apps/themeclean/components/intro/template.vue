<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-md-10 d-flex flex-column text-center">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <h1 class="pb-3" v-if="model.showtitle == 'true'"
      v-html="model.title"></h1>
      <h3 class="pb-3" v-if="model.showsubtitle == 'true'" v-html="model.subtitle"></h3>
      <p class="pb-3" v-if="model.showtext == 'true'" v-html="model.text"></p>
      <div class="d-flex justify-content-center" v-if="model.showbutton == 'true'">
        <a class="btn btn-lg btn-secondary m-2" role="button" v-for="(item,i) in model.buttons"
        :key="i" v-bind:href="item.buttonlink" v-html="item.buttontext" v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`"></a>
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
                return !(
                  this.model.showtitle === 'true' || 
                  this.model.showsubtitle === 'true' || 
                  this.model.showtext === 'true' || 
                  this.model.showbutton === 'true' 
                )
            }
        }
    }
</script>