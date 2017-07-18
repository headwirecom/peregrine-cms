<template>
  <div class="blog-header" v-bind:data-per-path="model.path">
    <div class="container">
      <h1 class="blog-title">{{model.title}}</h1>
      <p class="lead blog-description">{{model.description}}</p>
    </div>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

