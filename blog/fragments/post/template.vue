<template>
  <div class="blog-post" v-bind:data-per-path="model.path">
    <pagerender-vue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'before' }"></pagerender-vue-components-placeholder>
    <h2 class="blog-post-title">{{model.title}}</h2>
    <p class="blog-post-meta">{{model.date}} by
      <a href="#">{{model.author}}</a>
    </p>
    <pagerender-vue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'after' }"></pagerender-vue-components-placeholder>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

