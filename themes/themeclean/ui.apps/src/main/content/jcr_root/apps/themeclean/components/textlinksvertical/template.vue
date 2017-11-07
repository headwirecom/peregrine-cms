<template>
  <div class="col-12 col-md-3 d-flex flex-column">
    <h5 class="" v-html="model.title"></h5>
    <a v-for="(item,i) in model.links" :key="i" v-bind:href="item.link"
    v-html="item.text"></a>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

