<template>
  <nav class="blog-pagination" v-bind:data-per-path="model.path">
    <a class="btn btn-outline-primary" href="#">{{model.previous}}</a>
    <a class="btn btn-outline-secondary disabled" href="#">{{model.next}}</a>
  </nav>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

