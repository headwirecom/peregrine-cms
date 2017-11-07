<template>
  <themeclean-components-block v-bind:model="model">
    <div class="card-deck">
      <h1 v-if="editAndEmpty">Configure Here</h1>
      <div class="card p-0 col-12 col-md border-0" v-for="(item,i) in model.cards"
      :key="i" v-bind:style="`background-color:${model.cardcolor};`">
        <img class="card-img pb-1" v-bind:src="item.image">
        <div class="card-body">
          <h5 class="card-title" v-if="model.showtitle == 'true'" v-html="item.title"
          v-bind:style="`color:${item.color};`"></h5>
          <p v-if="model.showtext == 'true'" v-html="item.text"></p>
          <div class="text-center">
            <a class="btn btn-lg btn-primary" v-bind:href="item.buttonlink" v-html="item.buttontext"
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
            editAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                if(!this.model.cards) return true
                if(this.model.cards.length === 0) return true
                return false
            },
            editOne() {
                if(!$peregrineApp.isAuthorMode()) return false
                if(!this.model.cards) return true
                if(this.model.cards.length === 1) return true
                return false
            }
        }
    }
</script>