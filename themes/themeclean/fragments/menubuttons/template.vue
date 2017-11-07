<template>
  <div>
    <a class="btn btn-primary" v-for="(item,i) in model.buttons" :key="i"
    v-bind:href="item.buttonlink">
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

