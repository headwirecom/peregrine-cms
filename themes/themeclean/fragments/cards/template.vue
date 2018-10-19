<template>
  <themeclean-components-block v-bind:model="model">
    <div class="card-deck justify-content-center">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="col-12 col-md-6 col-lg p-0 pb-md-3"
      v-for="(item,i) in model.cards" :key="i">
        <div class="card p-0 border-0 d-flex flex-column justify-content-between h-100"
        v-bind:style="`background-color:${model.customcardcolor === 'true' ? model.cardcolor: ''};`"
        v-bind:class="{
            'bg-dark': model.customcardcolor !== 'true' &amp;&amp; model.colorscheme === 'light',
            'bg-light': model.customcardcolor !== 'true' &amp;&amp;  model.colorscheme === 'dark',
            'text-dark': (model.showcard === 'false' &amp;&amp; model.colorscheme === 'light') || (model.showcard === 'true' &amp;&amp; model.colorscheme === 'dark'),
            'text-light': (model.showcard === 'false' &amp;&amp; model.colorscheme === 'dark') || (model.showcard === 'true' &amp;&amp; model.colorscheme === 'light'),
            'bg-transparent': model.showcard === 'false'
        }">
          <div>
            <img v-bind:class="model.showcard == 'true' ? 'card-img pb-1' : 'card-img pb-3'"
            v-bind:src="$helper.pathToUrl(item.image)" v-bind:alt="item.imagealttext"
            v-if="item.image">
            <div v-bind:class="{
            'card-body': model.showcard === 'true',
            'px-3 p-md-0': model.showcard === 'false'
        }">
              <h5 class="card-title" v-if="model.showtitle == 'true'" v-html="item.title"
              v-bind:style="`color:${item.color};`"></h5>
              <p v-if="model.showtext == 'true'" v-html="item.text"></p>
            </div>
          </div>
          <div class="text-center" v-if="item.buttontext">
            <a class="btn mb-3" v-if="model.showbutton == 'true'" v-bind:href="$helper.pathToUrl(item.buttonlink)"
            v-bind:class="{
            'btn-lg': item.buttonsize === 'large',
            'btn-sm': item.buttonsize === 'small',
            'btn-primary': item.buttoncolor === 'primary',
            'btn-secondary': item.buttoncolor === 'secondary',
            'btn-success': item.buttoncolor === 'success',
            'btn-danger': item.buttoncolor === 'danger',
            'btn-warning': item.buttoncolor === 'warning',
            'btn-info': item.buttoncolor === 'info',
            'btn-light': item.buttoncolor === 'light',
            'btn-dark': item.buttoncolor === 'dark'
        }" v-html="item.buttontext"></a>
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
                //return (this.model.cards.length === 0)
                return this.$helper.areAllEmpty(this.model.cards)
            }
        }
    }
</script>

