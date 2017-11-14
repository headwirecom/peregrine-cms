<template>
  <div class="d-flex justify-content-center flex-column flex-sm-row">
    <a class="btn btn-lg btn-primary mb-2 m-sm-2" v-for="(item,i) in model.buttons"
    :key="i" v-bind:href="item.buttonlink" v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`">
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

