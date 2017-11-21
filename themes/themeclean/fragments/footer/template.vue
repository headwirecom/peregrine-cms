<template>
  <themeclean-components-block v-bind:model="model">
    <footer class="col-12">
      <div class="row justify-content-between text-center text-md-left">
        <div class="col-12 col-md-auto" v-if="model.showlogo === 'true'">
          <a v-bind:href="$helper.pathToUrl(model.logourl)">
            <img class="mb-3" alt="logo" v-bind:src="$helper.pathToUrl(model.logo)"
            v-bind:style="`height:${parseInt(model.logosize)}px;`">
          </a>
        </div>
        <div class="col-12 col-md" v-for="(item,i) in model.columns" :key="i">
          <h5 class="" v-if="item.title !== ''">{{item.title}}</h5>
          <div v-html="item.text"></div>
        </div>
      </div>
      <hr>
      <div class="row text-center text-md-left">
        <div class="col-md-6">
          <p>&#xA9; Copyright 2017 Peregrine - All Rights Reserved</p>
        </div>
        <themeclean-components-socialicons v-bind:model="model"></themeclean-components-socialicons>
      </div>
    </footer>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

