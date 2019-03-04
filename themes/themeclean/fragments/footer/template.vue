<template>
  <themeclean-components-block v-bind:model="model">
    <footer class="col-12">
      <div class="row justify-content-between text-center text-md-left">
        <div class="col-12 col-md-auto" v-if="model.showlogo === 'true' &amp;&amp; model.logo">
          <a v-bind:href="$helper.pathToUrl(model.logourl)">
            <img class="mb-3" v-bind:src="$helper.pathToUrl(model.logo)" v-bind:alt="model.logoalttext"
            v-bind:style="`height:${parseInt(model.logosize)}px;`">
          </a>
        </div>
        <div class="col-12 col-md pb-3 pb-md-0" v-for="(item,i) in model.columns"
        :key="i">
          <h5 class v-if="item.title !== ''">{{item.title}}</h5>
          <div v-if="item.text !== ''" v-html="item.text"></div>
        </div>
      </div>
      <hr>
      <div class="row text-center text-md-left py-2">
        <div class="col-md-6">
          <p v-html="model.copyright"></p>
        </div>
        <themeclean-components-socialicons v-bind:model="model"></themeclean-components-socialicons>
      </div>
      <div v-if="isEditAndEmpty">no content defined for component</div>
    </footer>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return this.$helper.areAllEmpty(this.model.showlogo === 'true', this.model.columns,  this.model.copyright, this.model.icons)
            }
        }
    }
</script>

