<template>
  <themeclean-components-block v-bind:model="model">
    <div class="card-deck">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="card p-0 col-12 col-md border-0 d-flex flex-column justify-content-between"
      v-for="(item,i) in model.cards" :key="i" v-bind:style="`background-color:${model.showcard == 'true' ? model.cardcolor : 'transparent'};`">
        <div>
          <img v-bind:class="model.showcard == 'true' ? 'card-img pb-1' : 'card-img pb-3'"
          v-bind:src="$helper.pathToUrl(item.image)" v-bind:alt="item.imagealttext"
          v-if="item.image">
          <div v-bind:class="model.showcard == 'true' ? 'card-body' : ''">
            <h5 class="card-title" v-if="model.showtitle == 'true'" v-html="item.title"
            v-bind:style="`color:${item.color};`"></h5>
            <p v-if="model.showtext == 'true'" v-html="item.text"></p>
          </div>
        </div>
        <div class="text-center" v-if="item.buttontext">
          <a class="btn btn-lg btn-primary mb-3" v-if="model.showbutton == 'true'"
          v-bind:href="$helper.pathToUrl(item.buttonlink)" v-html="item.buttontext"
          v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`"></a>
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
                //return (this.model.cards.length === 0)
                return this.$helper.areAllEmpty(this.model.cards)
            }
        }
    }
</script>

