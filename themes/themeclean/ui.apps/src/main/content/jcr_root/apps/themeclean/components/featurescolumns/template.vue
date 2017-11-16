<template>
  <themeclean-components-block v-bind:model="model">
    <div class="card-deck">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="card p-3 col-12 col-md border-0 bg-transparent"
      v-for="(item,i) in model.cards" :key="i">
        <img class="card-img pb-3" v-bind:src="$helper.pathToUrl(item.image)">
        <div>
          <h5 class="pb-2" v-if="model.showtitle == 'true'" v-html="item.title"></h5>
          <p v-if="model.showtext == 'true'" v-html="item.text"></p>
          <div class="text-center">
            <a class="btn btn-lg btn-primary" v-if="model.showbutton == 'true'"
            v-bind:href="$helper.pathToUrl(item.buttonlink)" v-html="item.buttontext"
            v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`"></a>
          </div>
        </div>
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
                //return this.model.cards.length === 0
                return this.$helper.areAllEmpty(this.model.cards)
            },
        }
    }
</script>