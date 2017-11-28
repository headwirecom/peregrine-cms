<template>
  <div class="d-flex justify-content-end flex-column flex-sm-row">
    <a class="btn btn-primary mb-2 ml-2 mr-0" v-for="(item,i) in model.buttons"
    :key="i" v-bind:href="$helper.pathToUrl(item.buttonlink)" v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`">
      <span></span>
      <span v-html="item.buttontext"></span>
    </a>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

