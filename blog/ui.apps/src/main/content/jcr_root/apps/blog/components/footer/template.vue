<template>
  <footer class="blog-footer" v-bind:data-per-path="model.path">
    <p v-html="model.text"></p>
    <p>
      <a href="#">Back to top</a>
    </p>
  </footer>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

