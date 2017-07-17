<template>
  <div v-bind:data-per-path="model.path" v-bind:class="model.style">
    <h4>{{model.title}}</h4>
    <p v-html="model.text"></p>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

