<template>
  <pcmsstock-components-block v-bind:model="model">
    <div class="col-12">
      <div class="row justify-content-between text-center text-md-left">
        <div class="col-12 col-md-3">
          <a v-bind:href="model.logourl">
            <img class="mb-3" alt="logo" v-bind:src="model.logo" v-bind:style="`height:${parseInt(model.logosize)}rem;`">
          </a>
        </div>
        <div class="col-12 col-md-3">
          <h5 class="" v-html="model.title1"></h5>
          <p v-html="model.text1"></p>
        </div>
        <div class="col-12 col-md-3">
          <h5 class="" v-html="model.title2"></h5>
          <p v-html="model.text2"></p>
        </div>
        <pcmsstock-components-textlinksvertical v-bind:model="model"></pcmsstock-components-textlinksvertical>
      </div>
      <hr>
      <div class="row text-center text-md-left">
        <div class="col-md-6">
          <p v-html="model.copyright"></p>
        </div>
        <pcmsstock-components-socialicons v-bind:model="model"></pcmsstock-components-socialicons>
      </div>
    </div>
  </pcmsstock-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

