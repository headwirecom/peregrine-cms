<template>
  <ul class="pager" v-bind:data-per-path="model.path">
    <li v-if="model.previous" class="previous">
      <a v-bind:href="model.previous  + '.html'">Prev</a>
    </li>
    <li v-if="model.next" class="next">
      <a v-bind:href="model.next + '.html'">Next</a>
    </li>
  </ul>
</template>

<script>
export default { props: ['model'] }
</script>

