<template>
  <div class="blog-masthead" v-bind:data-per-path="model.path">
    <div class="container">
      <nav class="nav blog-nav">
        <a class="nav-link active" href="#">Home</a>
        <a class="nav-link" href="#">New features</a>
        <a class="nav-link" href="#">Press</a>
        <a class="nav-link" href="#">New hires</a>
        <a class="nav-link" href="#">About</a>
      </nav>
    </div>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

