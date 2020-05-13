<template>
  <div class="blog-post" v-bind:data-per-path="model.path">
    <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'before' }"></pagerendervue-components-placeholder>
    <h2 class="blog-post-title">{{model.title}}</h2>
    <p class="blog-post-meta">{{model.date}} by
      <a href="#">{{model.author}}</a>
    </p>
    <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'after' }"></pagerendervue-components-placeholder>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

