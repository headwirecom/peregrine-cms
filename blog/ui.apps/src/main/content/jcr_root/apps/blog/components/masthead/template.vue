<template>
  <div class="blog-masthead" v-bind:data-per-path="model.path">
    <div class="container">
      <nav class="nav blog-nav">
        <a v-for="nav in model.navigation" class="nav-link" v-bind:href="nav.path + '.html'">{{nav.title}}</a>
      </nav>
    </div>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

