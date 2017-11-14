<template>
  <div class="ml-3">
    <a class="btn btn-lg btn-primary" v-for="(item,i) in model.buttons" :key="i"
    v-bind:href="item.buttonlink" v-bind:style="`backgroundColor:${item.buttoncolor};borderColor:${item.buttoncolor};`">
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

